/**
 * @class Receipt
 * @description Merepresentasikan data struk hasil ekstraksi AI.
 */
class Receipt {
  /**
   * @param {string} id
   * @param {string|null} merchant_name
   * @param {string} transaction_date
   * @param {string} transaction_time
   * @param {Array<Object>} items
   * @param {number} subtotal
   * @param {number|null} discount_amount
   * @param {number|null} additional_charges
   * @param {number|null} tax_amount
   * @param {number} final_total
   * @param {string|null} tender_type
   * @param {number|null} amount_paid
   * @param {number|null} change_given
   * @param {string|null} category_spending
   */
  constructor(
    id,
    merchant_name = null,
    transaction_date,
    transaction_time,
    items,
    subtotal,
    discount_amount = null,
    additional_charges = null,
    tax_amount = null,
    final_total,
    tender_type,
    amount_paid = null,
    change_given = null,
    category_spending
  ) {
    this.id = id;
    this.merchant_name = merchant_name;
    this.transaction_date = transaction_date;
    this.transaction_time = transaction_time;
    this.items = items;
    this.subtotal = subtotal;
    this.discount_amount = discount_amount;
    this.additional_charges = additional_charges;
    this.tax_amount = tax_amount;
    this.final_total = final_total;
    this.tender_type = tender_type;
    this.amount_paid = amount_paid;
    this.change_given = change_given;
    this.category_spending = category_spending;
  }

  /**
   * @returns {Object} Representasi objek Receipt untuk Firestore.
   */
  toFirestore() {
    return {
      id: this.id,
      merchant_name: this.merchant_name,
      transaction_date: this.transaction_date,
      transaction_time: this.transaction_time,
      items: this.items,
      subtotal: this.subtotal,
      discount_amount: this.discount_amount,
      additional_charges: this.additional_charges,
      tax_amount: this.tax_amount,
      final_total: this.final_total,
      tender_type: this.tender_type,
      amount_paid: this.amount_paid,
      change_given: this.change_given,
      category_spending: this.category_spending
    };
  }

  /**
   * Membuat instance Receipt dari data Firestore.
   * @param {Object} data - Data objek dari Firestore.
   * @returns {Receipt} Instance Receipt.
   */
  static fromFirestore(data) {
    return new Receipt(
      data.id,
      data.merchant_name,
      data.transaction_date,
      data.transaction_time,
      data.items,
      data.subtotal,
      data.discount_amount,
      data.additional_charges,
      data.tax_amount,
      data.final_total,
      data.tender_type,
      data.amount_paid,
      data.change_given,
      data.category_spending
    );
  }
}

module.exports = { Receipt };
