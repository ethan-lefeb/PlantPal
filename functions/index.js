const functions = require("firebase-functions");
const admin = require("firebase-admin");
const {onSchedule} = require("firebase-functions/v2/scheduler");
admin.initializeApp();

/**
 * Calculate number of full days between timestamps.
 * @param {number} now
 * @param {number} timestamp
 * @return {number}
 */
function daysBetween(now, timestamp) {
  if (!timestamp || timestamp === 0) {
    return Number.MAX_SAFE_INTEGER;
  }
  return Math.floor(
      (now - timestamp) / (1000 * 60 * 60 * 24),
  );
}

/**
 * Build all care alerts for a user's plants.
 * @param {string} userId
 * @return {Promise<Array<object>>}
 */
async function buildCareAlertsForUser(userId) {
  const plantsSnap = await admin
      .firestore()
      .collection("users")
      .doc(userId)
      .collection("plants")
      .get();

  const now = Date.now();
  const alerts = [];

  plantsSnap.forEach((doc) => {
    const plant = doc.data();
    const plantId = doc.id;

    /* WATER */
    const waterDays = daysBetween(now, plant.lastWatered);
    const waterInterval = plant.wateringFrequency || 7;
    if (waterDays >= waterInterval) {
      alerts.push({
        plantId: plantId,
        name: plant.commonName,
        type: "WATER",
        message:
          "💧 " + plant.commonName +
          " needs watering today.",
      });
    }

    /* FERTILIZER */
    const fertDays = daysBetween(now, plant.lastFertilized);
    const fertInterval = plant.fertilizerFrequency || 30;
    if (fertDays >= fertInterval) {
      alerts.push({
        plantId: plantId,
        name: plant.commonName,
        type: "FERTILIZE",
        message:
          "🌱 " + plant.commonName +
          " needs fertilizer.",
      });
    }

    /* ROTATION */
    const careProfile =
      plant.careProfile ? plant.careProfile : {};

    const rotateDays = daysBetween(
        now,
        careProfile.lastRotated,
    );

    const rotateInterval =
      careProfile.rotationFrequency || 14;

    if (rotateDays >= rotateInterval) {
      alerts.push({
        plantId: plantId,
        name: plant.commonName,
        type: "ROTATE",
        message:
          "🌀 " + plant.commonName +
          " should be rotated.",
      });
    }
  });

  return alerts;
}

/**
 * Send a notification through Firebase Admin SDK.
 * @param {string} token
 * @param {string} title
 * @param {string} body
 * @param {object=} data
 * @return {Promise<string>}
 */
async function sendNotification(token, title, body, data) {
  const message = {
    token: token,
    notification: {title: title, body: body},
    data: data || {},
  };

  return admin.messaging().send(message);
}

/**
 * Developer test notification endpoint.
 */
exports.sendTestNotification =
functions.https.onRequest(async (req, res) => {
  const userId = req.query.userId;

  if (!userId) {
    return res.status(400).send("Missing userId");
  }

  try {
    const userDoc = await admin
        .firestore()
        .collection("users")
        .doc(userId)
        .get();

    const token = userDoc.get("fcmToken");

    if (!token) {
      return res
          .status(404)
          .send("User has no fcmToken");
    }

    await sendNotification(
        token,
        "🌿 PlantPal Test",
        "This is a test reminder.",
        {type: "TEST_ALERT"},
    );

    return res.send("Test notification sent!");
  } catch (err) {
    console.error("Test error:", err);
    return res
        .status(500)
        .send("Failed: " + err.message);
  }
});

/**
 * Daily scheduler to send plant reminders.
 */
exports.dailyPlantCareReminders = onSchedule(
    {
      schedule: "every 24 hours",
      timeZone: "America/New_York",
    },
    async () => {
      console.log("🌤 Running daily plant care reminder job...");

      const usersSnap = await admin
          .firestore()
          .collection("users")
          .get();

      for (const userDoc of usersSnap.docs) {
        const userId = userDoc.id;
        const token = userDoc.get("fcmToken");

        if (!token) {
          console.log("⚠ No token for user " + userId);
          continue;
        }

        const alerts =
        await buildCareAlertsForUser(userId);

        if (alerts.length === 0) {
          console.log("⭐ No alerts for user " + userId);
          continue;
        }

        const title =
        "🌱 Your PlantPal Care Reminders";

        const body = alerts
            .map((a) => a.message)
            .slice(0, 4)
            .join("\n");

        const data = {
          type: "CARE_SUMMARY",
          totalAlerts: alerts.length.toString(),
        };

        try {
          await sendNotification(
              token,
              title,
              body,
              data,
          );
          console.log(
              "📨 Sent care reminders to " + userId,
          );
        } catch (err) {
          console.error(
              "❌ Failed to send for " + userId,
              err,
          );
        }
      }

      console.log("🌿 Daily reminder job complete.");
    },
);

/**
 * On-demand reminders sent via Worker button.
 */
exports.sendCareRemindersNow =
functions.https.onRequest(async (req, res) => {
  const userId = req.query.userId;

  if (!userId) {
    return res
        .status(400)
        .send("Missing userId");
  }

  try {
    const userDoc = await admin
        .firestore()
        .collection("users")
        .doc(userId)
        .get();

    const token = userDoc.get("fcmToken");

    if (!token) {
      return res
          .status(404)
          .send(
              "User " + userId +
          " has no token",
          );
    }

    const alerts =
      await buildCareAlertsForUser(userId);

    if (alerts.length === 0) {
      return res.send("No tasks due.");
    }

    const body = alerts
        .map((a) => a.message)
        .join("\n");

    await sendNotification(
        token,
        "PlantPal Care Reminders",
        body,
        {type: "CARE_SUMMARY"},
    );

    return res.send("Reminders sent.");
  } catch (err) {
    console.error(err);
    return res
        .status(500)
        .send(err.message);
  }
});
