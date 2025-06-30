/**
 * @class UserProfile
 * @description Merepresentasikan profil tambahan pengguna.
 */
class UserProfile {
  /**
   * @param {string} occupation
   * @param {number} income
   * @param {string} financialGoals
   * @param {string} [currency='IDR']
   * @param {Object} [additionalData={}]
   */
  constructor(occupation, income, financialGoals, currency = 'IDR', additionalData = {}) {
    this.occupation = occupation;
    this.income = income;
    this.financialGoals = financialGoals;
    this.currency = currency;
    this.additionalData = additionalData;
  }

  /**
   * @returns {Object} Representasi objek UserProfile untuk Firestore.
   */
  toFirestore() {
    return {
      occupation: this.occupation,
      income: this.income,
      financialGoals: this.financialGoals,
      currency: this.currency,
      ...this.additionalData
    };
  }

  /**
   * Membuat instance UserProfile dari data Firestore.
   * @param {Object} data - Data objek dari Firestore.
   * @returns {UserProfile} Instance UserProfile.
   */
  static fromFirestore(data) {
    const { occupation, income, financialGoals, currency, ...additionalData } = data;
    return new UserProfile(occupation, income, financialGoals, currency, additionalData);
  }
}


class User {
  /**
   * @param {string} uid - User ID unik dari Firebase Authentication.
   * @param {string} username - Nama pengguna.
   * @param {string} email - Alamat email pengguna (unik).
   * @param {Date} createdAt - Tanggal dan waktu akun dibuat.
   * @param {boolean} profileCompleted - Flag apakah profil tambahan sudah diisi.
   * @param {UserProfile|null} [userProfile=null] - Instance UserProfile jika sudah ada.
   */
  constructor(uid, username, email, createdAt, profileCompleted = false, userProfile = null) {
    this.uid = uid;
    this.username = username;
    this.email = email;
    this.createdAt = createdAt;
    this.profileCompleted = profileCompleted;
    this.userProfile = userProfile; // Akan menjadi instance UserProfile
  }

  /**
   * Mengonversi instance User menjadi objek plain untuk disimpan ke Firestore.
   * @returns {Object} Representasi objek User untuk Firestore.
   */
  toFirestore() {
    return {
      uid: this.uid,
      username: this.username,
      email: this.email,
      createdAt: this.createdAt,
      profileCompleted: this.profileCompleted,
      userProfile: this.userProfile ? this.userProfile.toFirestore() : null
    };
  }

  /**
   * Membuat instance User dari data Firestore.
   * @param {string} uid - User ID (biasanya didapat dari ID dokumen Firestore).
   * @param {Object} data - Data objek dari Firestore.
   * @returns {User} Instance User.
   */
  static fromFirestore(uid, data) {
    const { username, email, createdAt, profileCompleted, userProfile } = data;
    const profile = userProfile ? UserProfile.fromFirestore(userProfile) : null;
    // createdAt dari Firestore bisa berupa Timestamp, konversi ke Date object
    const createdDate = createdAt && createdAt.toDate ? createdAt.toDate() : createdAt;
    return new User(uid, username, email, createdDate, profileCompleted, profile);
  }
}

// Export kedua kelas
module.exports = { User, UserProfile };