package com.example.decompositionanalyzer

import com.google.mlkit.vision.label.ImageLabel

data class Material(
    val name: String,
    val decompositionTime: String,
    val impact: String,
    val alternatives: String
)

class MaterialDatabase {
    private val materials = mapOf(
        "plastic" to Material(
            name = "Plastic Item",
            decompositionTime = "450 years",
            impact = "High environmental impact. Can break down into microplastics.",
            alternatives = "Reusable containers, biodegradable materials"
        ),
        "paper" to Material(
            name = "Paper Item",
            decompositionTime = "1 month",
            impact = "Low environmental impact if not contaminated.",
            alternatives = "Digital alternatives, reusable materials"
        ),
        "glass" to Material(
            name = "Glass Item",
            decompositionTime = "1 million years",
            impact = "Non-biodegradable but recyclable indefinitely.",
            alternatives = "Reusable glass containers"
        ),
        "metal" to Material(
            name = "Metal Item",
            decompositionTime = "200-500 years",
            impact = "High energy to produce but infinitely recyclable.",
            alternatives = "Reusable containers"
        ),
        "food" to Material(
            name = "Food Item ",
            decompositionTime = "1-6 months",
            impact = "Biodegradable but produces methane in landfills.",
            alternatives = "Composting, better meal planning"
        ),
        "textile" to Material(
            name = "Textile/Clothing",
            decompositionTime = "1-5 years",
            impact = "Varies by material. Synthetic fabrics create microplastics.",
            alternatives = "Natural fibers, buying second-hand"
        )
    )

    private val materialKeywords = mapOf(
        "plastic" to listOf("bottle", "plastic", "container", "polymer"),
        "paper" to listOf("paper", "cardboard", "newspaper", "magazine"),
        "glass" to listOf("glass", "bottle", "window"),
        "metal" to listOf("can", "aluminum", "metal", "steel"),
        "food" to listOf("food", "fruit", "vegetable", "meat"),
        "textile" to listOf("cloth", "clothing", "fabric", "textile")
    )

    fun findMaterialFromLabels(labels: List<ImageLabel>): Material? {
        for (label in labels) {
            val text = label.text.lowercase() // Use lowercase() instead of toLowerCase()

            for ((material, keywords) in materialKeywords) {
                if (keywords.any { it in text }) {
                    return materials[material]
                }
            }
        }
        return null
    }
}