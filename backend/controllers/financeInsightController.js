const { GoogleGenAI, createUserContent } = require('@google/genai');

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

exports.getMonthlyFinanceInsights = async (req, res) => {
  try {
    const income = req.body.income;
    const monthReceipts = req.body.monthReceipts; // array transaksi bulan ini

    if (!Array.isArray(monthReceipts) || monthReceipts.length === 0) {
      return res.status(400).json({ message: "Data monthReceipts tidak valid atau kosong." });
    }

    const monthlySpendingJSON = JSON.stringify(monthReceipts, null, 2);

    console.log("Monthly Spending Data:", monthlySpendingJSON);

    const prompt = `
        Berikut adalah data transaksi bulanan seorang user:

        ini incomenya perbulannya ${income}

        ini data pengeluarannya:

        ${monthlySpendingJSON}

        Berdasarkan data tersebut, berikan:
        - saran (string): Saran keuangan umum berdasarkan pola pengeluaran.
        - peringatan (string, optional): Peringatan jika ada kategori yang terlalu besar.
        - rekomendasi_aksi (array of string): Beberapa tindakan yang bisa dilakukan user agar lebih hemat.

        Berikan hasil dalam format JSON seperti contoh berikut:

        {
            "saran": "Pengeluaran Anda cukup besar di kategori makanan. Pertimbangkan untuk memasak di rumah." || null,
            "peringatan": "Pengeluaran Anda di kategori hiburan melebihi 30% dari total pengeluaran bulan ini." || null,
            "rekomendasi_aksi": [
                "Buat anggaran bulanan untuk setiap kategori.",
                "Gunakan aplikasi pencatat pengeluaran secara rutin.",
                "Kurangi belanja impulsif."
            ]
        }
`;

    const result = await ai.models.generateContent({
      model: "gemini-2.0-flash",
      contents: createUserContent([prompt]),
      config: {
        temperature: 0.4,
        topK: 1,
        topP: 1,
        maxOutputTokens: 1024
      }
    });

    let recommendationText = result.text;

    console.log("AI Response:", recommendationText);

    if (recommendationText.startsWith("```json")) {
      recommendationText = recommendationText.slice(7, recommendationText.lastIndexOf("```")).trim();
    } else if (recommendationText.startsWith("```")) {
      recommendationText = recommendationText.slice(3, recommendationText.lastIndexOf("```")).trim();
    }

    let recommendationData = {};
    try {
      recommendationData = JSON.parse(recommendationText);
    } catch (e) {
      return res.status(500).json({
        message: "Gagal mengurai output dari Gemini.",
        rawAiResponse: recommendationText,
        error: e.message
      });
    }

    return res.status(200).json({
      message: "Insight berhasil dihasilkan.",
      insights: recommendationData
    });

  } catch (error) {
    console.error("Error generating financial insights:", error);
    return res.status(500).json({ message: "Terjadi kesalahan internal.", error: error.message });
  }
};
