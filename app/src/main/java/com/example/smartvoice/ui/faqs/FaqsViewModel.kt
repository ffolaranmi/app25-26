package com.example.smartvoice.ui.faqs

import androidx.lifecycle.ViewModel

data class FaqItem(val question: String, val answer: String)

class FaqsViewModel : ViewModel() {
    val faqs = listOf(
        FaqItem(
            "What is SmartVoice?",
            "SmartVoice is a mobile application designed to help detect the presence of Recurrent Respiratory Papillomatosis (RRP) in children. The app allows parents or guardians to record short voice samples and analyse them using deep learning models to identify potential traces of RRP. The goal is to support earlier awareness and guide families seek medical advice if needed."
        ),
        FaqItem(
            "What is RRP?",
            "Recurrent Respiratory Papillomatosis (RRP) is a rare condition caused by the human papillomavirus (HPV). It leads to the growth of small, wart-like tumours in the larynx. These growths can cause symptoms such as hoarseness, breathing difficulties, or changes in voice quality."
        ),
        FaqItem(
            "I think my child may have symptoms of RRP. What should I do?",
            "SmartVoice is not a 100% accurate medical diagnostic tool. If you notice persistent hoarseness, breathing difficulties, or other unusual voice changes in your child, you should consult a qualified healthcare professional. A doctor or ENT specialist will be able to assess symptoms and provide verified medical advice."
        ),
        FaqItem(
            "Is SmartVoice a medical diagnosis?",
            "No. SmartVoice provides supportive voice analysis but does not provide a medical diagnosis. The results are intended to help raise awareness and encourage users to seek professional medical advice if necessary."
        ),
        FaqItem(
            "Where can I find more information about RRP?",
            "You can find reliable information about RRP from healthcare providers, ENT specialists, and trusted medical organisations. If you are concerned about symptoms, it is always best to speak with your locals healthcare professional for accurate guidance and support."
        )
    )
}