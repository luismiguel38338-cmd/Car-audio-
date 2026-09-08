package com.example.model

data class BrazilianBox(
    val id: String,
    val name: String,
    val category: String,
    val speakerSizeInches: Float,
    val numSpeakers: Int,
    val netLiters: Float,
    val grossLiters: Float,
    val tuningHz: Float,
    val dimensionsCm: String,
    val widthCm: Float,
    val heightCm: Float,
    val depthCm: Float,
    val mdfThicknessMm: Int,
    val portType: String,
    val portDimensions: String,
    val hpfRecHz: Float,
    val lpfRecHz: Float,
    val powerHandlingRms: String,
    val woodCutList: List<String>,
    val acousticDescription: String,
    val tuningTip: String,
    val iconTag: String
)

object BrazilianBoxPresets {
    val list: List<BrazilianBox> = listOf(
        // 1. Cajón Brasileño de Uno (Caixa Bob Esponja 1x Sub/Woofer)
        BrazilianBox(
            id = "box_bob_1x",
            name = "Caixa Bob Esponja 1x Woofer",
            category = "1 Woofer / Bob Trio",
            speakerSizeInches = 12f,
            numSpeakers = 1,
            netLiters = 55.0f,
            grossLiters = 66.5f,
            tuningHz = 45.0f,
            dimensionsCm = "90 x 44 x 42 cm",
            widthCm = 44f,
            heightCm = 90f,
            depthCm = 42f,
            mdfThicknessMm = 18,
            portType = "Ducto Régua Inferior (Slot Port)",
            portDimensions = "Alto: 6 cm, Ancho: 40.4 cm, Profundidad: 28 cm",
            hpfRecHz = 35.0f,
            lpfRecHz = 110.0f,
            powerHandlingRms = "600W - 1500W RMS",
            woodCutList = listOf(
                "2x Laterales: 90.0 x 42.0 cm (MDF 18mm)",
                "1x Fondo: 90.0 x 40.4 cm",
                "1x Tapa Superior e Inferior: 44.0 x 42.0 cm",
                "1x Frente Baffle Sub: 45.0 x 40.4 cm",
                "1x Frente Médio/Driver: 41.4 x 40.4 cm",
                "1x Separador interno de cámara (Médio): 40.4 x 26.0 cm",
                "1x Tabla del ducto régua: 40.4 x 28.0 cm"
            ),
            acousticDescription = "El clásico sistema brasileño Caixa Bob de 1 vía de bajo con compartimento superior integrado para médios de 8\", driver fenólico (corneta) y super tweeter. Ideal para sonido residencial o maletero.",
            tuningTip = "Sintonía en 45Hz: ofrece graves profundos y retumbantes para reggaetón, funk brasileño y trap, con excelente respuesta transitoria.",
            iconTag = "BOB_1X"
        ),

        // 2. Cajón de 8 Pulgadas (Médios Graves Pancadão)
        BrazilianBox(
            id = "box_medio_8",
            name = "Caixa Canhão Médio 8\" Pancadão",
            category = "Médios de 8\"",
            speakerSizeInches = 8f,
            numSpeakers = 2,
            netLiters = 16.5f, // por cámara
            grossLiters = 21.0f,
            tuningHz = 95.0f,
            dimensionsCm = "28 x 56 x 32 cm (Doble 8\")",
            widthCm = 56f,
            heightCm = 28f,
            depthCm = 32f,
            mdfThicknessMm = 15,
            portType = "Ducto Triangular Pancadão",
            portDimensions = "4x Triángulos esquineros de 5x5 cm x 14 cm prof.",
            hpfRecHz = 120.0f,
            lpfRecHz = 1200.0f,
            powerHandlingRms = "350W - 800W RMS por cono",
            woodCutList = listOf(
                "2x Tapa Superior e Inferior: 56.0 x 32.0 cm (MDF 15mm)",
                "2x Laterales: 28.0 x 32.0 cm",
                "1x Trasera: 53.0 x 25.0 cm",
                "1x Baffle Frontal: 53.0 x 25.0 cm (2 orificios de 18.2 cm)",
                "1x Divisor central de cámaras: 29.0 x 25.0 cm",
                "8x Piezas triangulares de ducto: 7.0 x 14.0 cm"
            ),
            acousticDescription = "Diseño de cámara sellada con sintonía de desahogo triangular optimizado para el 'ataque de pecho' (Pancadão). Proyecta la voz con altísima presión y claridad a más de 50 metros.",
            tuningTip = "Sintonía en 95Hz-105Hz: corta en el DSP con HPF en 120Hz 24dB/oct para evitar excursión mecánica destructiva a altos volúmenes.",
            iconTag = "MEDIO_8"
        ),

        // 3. Cajón de 15 Pulgadas (Euclides Pancadão Som Automotivo)
        BrazilianBox(
            id = "box_euclides_15",
            name = "Caixa Euclides 15\" Som Automotivo",
            category = "Pancadão de 15\"",
            speakerSizeInches = 15f,
            numSpeakers = 1,
            netLiters = 95.0f,
            grossLiters = 118.0f,
            tuningHz = 55.0f,
            dimensionsCm = "52 x 52 x 56 cm",
            widthCm = 52f,
            heightCm = 52f,
            depthCm = 56f,
            mdfThicknessMm = 18,
            portType = "Deflector Inclinado V-Shape Euclides",
            portDimensions = "Garganta cónica con rampa acústica a 45°",
            hpfRecHz = 42.0f,
            lpfRecHz = 160.0f,
            powerHandlingRms = "1500W - 3500W RMS",
            woodCutList = listOf(
                "2x Laterales exteriores: 56.0 x 52.0 cm (MDF 18mm naval)",
                "2x Superior e Inferior: 52.0 x 52.4 cm",
                "1x Panel posterior: 48.4 x 48.4 cm",
                "1x Baffle frontal en V: 52.0 x 48.4 cm inclinado",
                "2x Deflectores Euclides: 32.0 x 48.4 cm en ángulo 45°",
                "4x Refuerzos internos antirresonancia: 5.0 x 48.4 cm"
            ),
            acousticDescription = "El diseño Euclides es la leyenda de los Paredões de Brasil. La compresión acústica frontal en V multiplica el rendimiento del cono de 15\" proyectando el bajo a distancias increíbles sin fatiga.",
            tuningTip = "Sintonía en 55Hz: perfecta para graves agresivos de forró, electrónica y pancadão pesado. Añade pegamento poliuretano y tornillos cada 8 cm.",
            iconTag = "EUCLIDES_15"
        ),

        // 4. Cajón de 6 Pulgadas (Médios de Voz / Cornetera)
        BrazilianBox(
            id = "box_medio_6",
            name = "Caixa Corneteira Médios 6\"",
            category = "Médios de 6\"",
            speakerSizeInches = 6f,
            numSpeakers = 2,
            netLiters = 8.0f, // por cámara
            grossLiters = 11.0f,
            tuningHz = 140.0f,
            dimensionsCm = "22 x 46 x 22 cm",
            widthCm = 46f,
            heightCm = 22f,
            depthCm = 22f,
            mdfThicknessMm = 15,
            portType = "Cámara Acústica con Micro-Ducto",
            portDimensions = "2x Tubos de 2 pulgadas (5 cm x 8 cm de largo)",
            hpfRecHz = 160.0f,
            lpfRecHz = 3500.0f,
            powerHandlingRms = "150W - 400W RMS por cono",
            woodCutList = listOf(
                "2x Tapas Superior e Inferior: 46.0 x 22.0 cm (MDF 15mm)",
                "2x Laterales: 22.0 x 19.0 cm",
                "1x Fondo Trasero: 43.0 x 19.0 cm",
                "1x Frente Baffle: 43.0 x 19.0 cm (2 orificios de 14.5 cm)",
                "1x Separador central estanco: 19.0 x 19.0 cm"
            ),
            acousticDescription = "Diseñada especialmente para medios vocales nítidos y sin distorsión en sistemas de 3 y 4 vías brasileños. Su tamaño compacto permite montarla en la parte alta del maletero o sombrerera.",
            tuningTip = "Sintonía en 140Hz: realza el brillo de las voces masculinas y femeninas sin sobrecalentar las bobinas del parlante de 6\".",
            iconTag = "MEDIO_6"
        ),

        // 5. Cajón Bajo de 6 Pulgadas (Mini Subwoofer / Mini Bob)
        BrazilianBox(
            id = "box_sub_6",
            name = "Caixa Mini Subwoofer Bajo 6\"",
            category = "Bajo de 6\" (Mini Sub)",
            speakerSizeInches = 6.5f,
            numSpeakers = 1,
            netLiters = 14.5f,
            grossLiters = 18.2f,
            tuningHz = 42.0f,
            dimensionsCm = "26 x 38 x 30 cm",
            widthCm = 38f,
            heightCm = 26f,
            depthCm = 30f,
            mdfThicknessMm = 15,
            portType = "Ducto Laberinto (Folded Slot Port)",
            portDimensions = "Ancho: 3.5 cm, Alto: 23.0 cm, Recorrido laberinto: 58 cm",
            hpfRecHz = 36.0f,
            lpfRecHz = 95.0f,
            powerHandlingRms = "200W - 500W RMS",
            woodCutList = listOf(
                "2x Tapas Superior e Inferior: 38.0 x 30.0 cm (MDF 15mm)",
                "2x Laterales: 26.0 x 27.0 cm",
                "1x Tapa Posterior: 35.0 x 23.0 cm",
                "1x Frente Baffle Sub: 31.5 x 23.0 cm (orificio 15.0 cm)",
                "1x Pared del Laberinto 1: 20.0 x 23.0 cm",
                "1x Pared del Laberinto 2: 14.0 x 23.0 cm"
            ),
            acousticDescription = "Ingeniería de ducto laberinto para extraer graves increíbles de 40Hz a partir de un transductor compacto de 6 o 6.5 pulgadas. La tendencia número 1 en Mini Caixas Bob portátiles de Brasil.",
            tuningTip = "Sintonía profunda en 42Hz: el largo recorrido del laberinto carga el cono y crea una ilusión de un subwoofer mucho más grande con cero soplo de aire.",
            iconTag = "SUB_6"
        )
    )
}
