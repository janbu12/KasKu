const { User, UserProfile } = require("../models/User");

// Get all users
exports.getAllUser = async (req, res) => {
  try {
    const db = req.db;
    const usersSnapshot = await db.collection('users').get();
    const users = [];
    usersSnapshot.forEach(doc => {
      users.push(User.fromFirestore(doc.id, doc.data()));
    });
    res.status(200).json(users);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch users.", error: error.message });
  }
};

exports.getMyProfile = async (req, res) => {
  const authenticatedUserUid = req.user.uid;
  const db = req.db;

  try {
    const userDoc = await db.collection('users').doc(authenticatedUserUid).get();
    if (!userDoc.exists) {
      return res.status(404).json({ message: "User not found." });
    }
    const user = User.fromFirestore(userDoc.id, userDoc.data());
    const filteredUser = {
      uid: user.uid,
      username: user.username,
      email: user.email,
      createdAt: user.createdAt,
      profileCompleted: user.profileCompleted,
      userProfile: user.userProfile ?? null
    };
    res.status(200).json(filteredUser);
  } catch (error) {
    res.status(500).json({ message: "Failed to fetch user profile.", error: error.message });
  }
};

exports.updateMyProfile = async (req, res) => {
  const authenticatedUserUid = req.user.uid;
  const { occupation, income, financialGoals, currency, ...additionalData } = req.body;
  const db = req.db;

  try {
      const userDocRef = db.collection('users').doc(authenticatedUserUid);
      const userDoc = await userDocRef.get();

      if (!userDoc.exists) {
          return res.status(404).send({ message: 'User not found.' });
      }

      if (!occupation || income === undefined || !financialGoals) {
          return res.status(400).send({ message: 'Occupation, income, and financial goals are required for profile update.' });
      }

      const newUserProfile = new UserProfile(occupation, income, financialGoals, currency, additionalData);

      await userDocRef.update({
          userProfile: newUserProfile.toFirestore(),
          profileCompleted: true
      });

      res.status(200).send({ message: 'User profile updated successfully!', updatedProfile: newUserProfile.toFirestore() });

  } catch (error) {
      console.error(`Error updating user profile for UID ${uid}:`, error);
      res.status(500).send({ message: "Failed to update user profile.", error: error.message });
  }
};