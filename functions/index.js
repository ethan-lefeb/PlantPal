
const {onSchedule} = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendCareReminders = onSchedule("every 24 hours", async () => {
  const db = admin.firestore();
  const users = await db.collection("users").get();

  for (const userDoc of users.docs) {
    const userId = userDoc.id;
    const token = userDoc.data().fcmToken;
    if (!token) continue;

    const plantsSnap = await db
        .collection("users")
        .doc(userId)
        .collection("plants")
        .get();

    const now = Date.now();

    for (const plantDoc of plantsSnap.docs) {
      const plant = plantDoc.data();
      const lastWatered = plant.lastWatered || 0;
      const frequency = plant.wateringFrequency || 7;
      const msSince = now - lastWatered;
      const due = frequency * 24 * 60 * 60 * 1000;

      if (msSince >= due) {
        const message = {
          token,
          notification: {
            title: `💧 Time to water ${plant.commonName}`,
            body: `${plant.commonName} is due for watering today!`,
          },
        };
        await admin.messaging().send(message);
      }
    }
  }

  return null;
});
