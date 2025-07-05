const path = require('path');
const fs = require('fs');
const multer = require('multer');
const { GoogleGenAI, createUserContent, createPartFromUri } = require('@google/genai');
const axios = require('axios');

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

exports.uploadStrukHandler = async (req, res) => {
  if (!req.file) {
    return res.status(400).send({ message: 'No image file uploaded.' });
  }

  const filePath = req.file.path;
  const originalFileName = req.file.originalname;

  try {
    const myfile = await ai.files.upload({
        file: path.join(filePath),
        config: { mimeType: "image/jpeg" },
    });

    const result = await ai.models.generateContent({
        model: "gemini-2.0-flash",
        contents: createUserContent([
            createPartFromUri(myfile.uri, myfile.mimeType),
            "\n\n",
            `Extract the following details from this receipt in JSON format.
                Provide the output with these parameters:
                - merchant_name (string): Name of the store/merchant.
                - transaction_date (string, format YYYY-MM-DD): Date of the transaction.
                - transaction_time (string, format HH:MM): Time of the transaction.
                - items (array of objects): List of purchased items.
                    - item_name (string): Name of the item.
                    - quantity (number): Quantity of the item.
                    - unit_price (number): Price per unit of the item.
                    - total_price_item (number): Total price for that specific item (quantity * unit_price).
                - subtotal (number): Total amount before tax, discount, or additional charges.
                - discount_amount (number, optional, default null): Total amount of discounts applied.
                - additional_charges (number, optional, default null): Any additional fees like service charge, packaging fee, etc.
                - tax_amount (number, optional, default null): Total tax amount (e.g., PPN).
                - final_total (number): The grand total amount paid after all calculations.
                - tender_type (string, optional, default null): How payment was made (e.g., "Cash", "Card").
                - amount_paid (number, optional, default null): The amount given by the customer.
                - change_given (number, optional, default null): The change returned to the customer.

                If any field is not found, use null for its value.
                Example format:
                {
                "merchant_name": "Indomaret",
                "transaction_date": "2025-07-01",
                "transaction_time": "13:50",
                "items": [
                    {
                    "item_name": "MARLBORO BLACK 12'S",
                    "quantity": 1,
                    "unit_price": 26200,
                    "total_price_item": 26200
                    },
                    {
                    "item_name": "ULTRA SLIM STRAW 200",
                    "quantity": 1,
                    "unit_price": 6100,
                    "total_price_item": 6100
                    }
                ],
                "subtotal": 32300,
                "discount_amount": null,
                "additional_charges": null,
                "tax_amount": 605,
                "final_total": 31695,
                "tender_type": "TUNAI",
                "amount_paid": 50000,
                "change_given": 17700
                }`,
        ]),
        config: {
            temperature: 0.1,
            topK: 1,
            topP: 1,
            maxOutputTokens: 2048,
        }
    });

    const responseText = result.text;

    let structuredData = {};
    let rawGeminiText = responseText;

    if (rawGeminiText.startsWith("```json")) {
        rawGeminiText = rawGeminiText.substring(7, rawGeminiText.lastIndexOf("```")).trim();
    } else if (rawGeminiText.startsWith("```")) {
        rawGeminiText = rawGeminiText.substring(3, rawGeminiText.lastIndexOf("```")).trim();
    }

    try {
        structuredData = JSON.parse(rawGeminiText);
    } catch (jsonError) {
        res.status(500).send({
            message: "Failed to parse structured data from AI. Raw AI response provided.",
            fileName: originalFileName,
            rawAiResponse: responseText,
            error: jsonError.message
        });
        fs.unlink(filePath, (err) => {
            if (err) console.error('Error deleting temp file:', err);
        });
        return;
    }
    
    res.status(200).send({
      message: 'Image processed successfully and data extracted!',
      fileName: originalFileName,
      structuredData: structuredData,
      rawGeminiText: responseText
    });

    fs.unlink(filePath, (err) => {
        if (err) console.error('Error deleting temp file:', err);
    });

  } catch (error) {
    fs.unlink(filePath, (err) => {
        if (err) console.error('Error deleting temp file:', err);
    });

    if (error instanceof multer.MulterError) {
        if (error.code === 'LIMIT_FILE_SIZE') {
            return res.status(400).send({ message: 'File size too large. Max 5MB allowed.' });
        }
    }
    res.status(500).send({ message: 'Failed to process image.', error: error.message });
  }
};
