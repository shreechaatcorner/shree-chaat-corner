const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

// Sends an alert to the owner Android app when a new Firestore order is created.
// The Android app subscribes to this topic after Firebase is initialized.
exports.notifyOwnerOnNewOrder = onDocumentCreated(
  { document: "orders/{orderId}", region: "asia-south1" },
  async (event) => {
    const order = event.data && event.data.data();
    if (!order) return;

    const orderId = event.params.orderId;
    const customer = typeof order.name === "string" && order.name.trim()
      ? order.name.trim()
      : "A customer";
    const total = Number(order.total);
    const totalText = Number.isFinite(total) && total >= 0
      ? ` · Total ₹${total}`
      : "";

    await getMessaging().send({
      topic: "shree_chaat_owner_orders",
      notification: {
        title: "New Shree Chaat Corner order!",
        body: `${customer} placed order #${orderId}${totalText}`,
      },
      data: {
        type: "new_order",
        orderId: String(orderId),
      },
      android: {
        priority: "high",
        notification: { channelId: "shree_chaat_owner_updates" },
      },
    });
  }
);
