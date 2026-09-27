package com.example.data.geo

import com.example.data.model.GeoPoint
import com.example.data.model.RoadSegment
import com.example.data.model.StateProgress

object MalaysiaRoadNetwork {

    val INITIAL_ROAD_SEGMENTS: List<RoadSegment> = listOf(
        // === SELANGOR & KUALA LUMPUR ===
        RoadSegment(
            id = "MY-FED-01",
            name = "Federal Highway (Route 2) - Subang to PJ",
            code = "Route 2",
            state = "Selangor",
            city = "Petaling Jaya",
            lengthKm = 12.4,
            pointsJson = "3.082,101.583;3.090,101.605;3.102,101.628;3.111,101.649;3.120,101.668",
            isExplored = true,
            exploredAt = 1716500000000L,
            timesDriven = 42
        ),
        RoadSegment(
            id = "MY-FED-02",
            name = "Federal Highway (Route 2) - Klang to Shah Alam",
            code = "Route 2",
            state = "Selangor",
            city = "Klang",
            lengthKm = 14.8,
            pointsJson = "3.045,101.448;3.055,101.485;3.068,101.520;3.076,101.555;3.082,101.583",
            isExplored = true,
            exploredAt = 1716200000000L,
            timesDriven = 19
        ),
        RoadSegment(
            id = "MY-FED-03",
            name = "Federal Highway (Route 2) - PJ to KL Mid Valley",
            code = "Route 2",
            state = "Kuala Lumpur",
            city = "Kuala Lumpur",
            lengthKm = 6.2,
            pointsJson = "3.120,101.668;3.123,101.678;3.127,101.688;3.134,101.696",
            isExplored = true,
            exploredAt = 1716700000000L,
            timesDriven = 55
        ),
        RoadSegment(
            id = "MY-E1-01",
            name = "North-South Expressway E1 - Rawang to Tanjung Malim",
            code = "E1",
            state = "Selangor",
            city = "Rawang",
            lengthKm = 46.5,
            pointsJson = "3.315,101.575;3.420,101.550;3.550,101.530;3.680,101.515",
            isExplored = true,
            exploredAt = 1715800000000L,
            timesDriven = 8
        ),
        RoadSegment(
            id = "MY-E1-02",
            name = "North-South Expressway E1 - Bukit Lanjan to Rawang",
            code = "E1",
            state = "Selangor",
            city = "Petaling Jaya",
            lengthKm = 18.2,
            pointsJson = "3.170,101.605;3.210,101.595;3.260,101.585;3.315,101.575",
            isExplored = true,
            exploredAt = 1716100000000L,
            timesDriven = 24
        ),
        RoadSegment(
            id = "MY-E2-01",
            name = "North-South Expressway E2 - Sungai Besi to Bangi",
            code = "E2",
            state = "Selangor",
            city = "Bangi",
            lengthKm = 21.0,
            pointsJson = "3.065,101.708;2.990,101.745;2.925,101.770",
            isExplored = true,
            exploredAt = 1715900000000L,
            timesDriven = 14
        ),
        RoadSegment(
            id = "MY-E2-02",
            name = "North-South Expressway E2 - Bangi to Seremban",
            code = "E2",
            state = "Negeri Sembilan",
            city = "Seremban",
            lengthKm = 38.5,
            pointsJson = "2.925,101.770;2.850,101.830;2.780,101.890;2.720,101.940",
            isExplored = true,
            exploredAt = 1715600000000L,
            timesDriven = 6
        ),
        RoadSegment(
            id = "MY-E8-01",
            name = "Karak Expressway E8 - Gombak Toll to Genting Sempah",
            code = "E8",
            state = "Selangor",
            city = "Gombak",
            lengthKm = 21.5,
            pointsJson = "3.238,101.735;3.285,101.765;3.330,101.780;3.360,101.792",
            isExplored = false, // Unexplored adventure!
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-E8-02",
            name = "Karak Expressway E8 - Genting Sempah to Bentong",
            code = "E8",
            state = "Pahang",
            city = "Bentong",
            lengthKm = 27.0,
            pointsJson = "3.360,101.792;3.420,101.840;3.480,101.885;3.525,101.912",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-E20-01",
            name = "Maju Expressway (MEX E20) - KL City to Seri Kembangan",
            code = "E20",
            state = "Kuala Lumpur",
            city = "Kuala Lumpur",
            lengthKm = 14.2,
            pointsJson = "3.142,101.718;3.105,101.705;3.060,101.692;3.010,101.685",
            isExplored = true,
            exploredAt = 1716300000000L,
            timesDriven = 31
        ),
        RoadSegment(
            id = "MY-E20-02",
            name = "Maju Expressway (MEX E20) - Seri Kembangan to Putrajaya",
            code = "E20",
            state = "Selangor",
            city = "Putrajaya",
            lengthKm = 11.8,
            pointsJson = "3.010,101.685;2.970,101.680;2.930,101.682;2.915,101.650",
            isExplored = true,
            exploredAt = 1716400000000L,
            timesDriven = 22
        ),
        RoadSegment(
            id = "MY-DASH-01",
            name = "Damansara-Shah Alam Elevated Expressway (DASH)",
            code = "E31",
            state = "Selangor",
            city = "Kota Damansara",
            lengthKm = 20.1,
            pointsJson = "3.135,101.492;3.145,101.530;3.155,101.580;3.165,101.632",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-DUKE-01",
            name = "Duta-Ulu Kelang Expressway (DUKE E33)",
            code = "E33",
            state = "Kuala Lumpur",
            city = "Kuala Lumpur",
            lengthKm = 18.0,
            pointsJson = "3.178,101.662;3.195,101.685;3.202,101.720;3.205,101.755",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-LDP-01",
            name = "Damansara-Puchong Expressway (LDP E11)",
            code = "E11",
            state = "Selangor",
            city = "Puchong",
            lengthKm = 24.5,
            pointsJson = "3.158,101.615;3.115,101.608;3.072,101.612;3.025,101.620;2.980,101.618",
            isExplored = true,
            exploredAt = 1716600000000L,
            timesDriven = 38
        ),

        // === PENANG & PERAK ===
        RoadSegment(
            id = "MY-E36-01",
            name = "Penang First Bridge (Jambatan Pulau Pinang)",
            code = "E36",
            state = "Penang",
            city = "Georgetown",
            lengthKm = 13.5,
            pointsJson = "5.355,100.395;5.352,100.355;5.350,100.315",
            isExplored = true,
            exploredAt = 1714500000000L,
            timesDriven = 4
        ),
        RoadSegment(
            id = "MY-E36-02",
            name = "Sultan Abdul Halim Muadzam Shah Bridge (Penang Second Bridge)",
            code = "E28",
            state = "Penang",
            city = "Batu Kawan",
            lengthKm = 24.0,
            pointsJson = "5.265,100.415;5.275,100.355;5.285,100.295",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-PEN-01",
            name = "Gurney Drive to Batu Ferringhi Coastal Pass",
            code = "Route 6",
            state = "Penang",
            city = "Batu Ferringhi",
            lengthKm = 16.2,
            pointsJson = "5.435,100.310;5.455,100.295;5.470,100.250",
            isExplored = true,
            exploredAt = 1714600000000L,
            timesDriven = 2
        ),
        RoadSegment(
            id = "MY-E1-03",
            name = "North-South Expressway E1 - Ipoh to Kuala Kangsar",
            code = "E1",
            state = "Perak",
            city = "Ipoh",
            lengthKm = 40.0,
            pointsJson = "4.590,101.090;4.680,101.020;4.770,100.930",
            isExplored = true,
            exploredAt = 1714800000000L,
            timesDriven = 5
        ),
        RoadSegment(
            id = "MY-E1-04",
            name = "North-South Expressway E1 - Kuala Kangsar to Taiping",
            code = "E1",
            state = "Perak",
            city = "Taiping",
            lengthKm = 34.0,
            pointsJson = "4.770,100.930;4.820,100.830;4.855,100.735",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),

        // === MELAKA & JOHOR ===
        RoadSegment(
            id = "MY-E2-03",
            name = "North-South Expressway E2 - Seremban to Ayer Keroh",
            code = "E2",
            state = "Melaka",
            city = "Melaka City",
            lengthKm = 72.0,
            pointsJson = "2.720,101.940;2.550,102.120;2.285,102.280",
            isExplored = true,
            exploredAt = 1715000000000L,
            timesDriven = 3
        ),
        RoadSegment(
            id = "MY-E2-04",
            name = "North-South Expressway E2 - Ayer Keroh to Yong Peng",
            code = "E2",
            state = "Johor",
            city = "Yong Peng",
            lengthKm = 78.0,
            pointsJson = "2.285,102.280;2.050,102.570;1.860,103.060",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-E2-05",
            name = "North-South Expressway E2 - Kulai to Johor Bahru Skudai",
            code = "E2",
            state = "Johor",
            city = "Johor Bahru",
            lengthKm = 32.0,
            pointsJson = "1.660,103.600;1.540,103.660;1.465,103.755",
            isExplored = true,
            exploredAt = 1715200000000L,
            timesDriven = 7
        ),
        RoadSegment(
            id = "MY-EDL-01",
            name = "Eastern Dispersal Link (EDL E14) - Pandan to CIQ",
            code = "E14",
            state = "Johor",
            city = "Johor Bahru",
            lengthKm = 8.1,
            pointsJson = "1.520,103.775;1.490,103.768;1.462,103.762",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),

        // === EAST COAST (PAHANG, TERENGGANU, KELANTAN) ===
        RoadSegment(
            id = "MY-E8-03",
            name = "East Coast Expressway E8 - Bentong to Kuantan",
            code = "E8",
            state = "Pahang",
            city = "Kuantan",
            lengthKm = 174.0,
            pointsJson = "3.525,101.912;3.620,102.400;3.720,102.850;3.815,103.325",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-R3-01",
            name = "Federal Route 3 - Kuantan Coastal Scenic Highway to Cherating",
            code = "Route 3",
            state = "Pahang",
            city = "Cherating",
            lengthKm = 48.0,
            pointsJson = "3.815,103.325;3.980,103.360;4.125,103.390",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-R3-02",
            name = "Federal Route 3 - Kemaman to Kuala Terengganu",
            code = "Route 3",
            state = "Terengganu",
            city = "Kuala Terengganu",
            lengthKm = 138.0,
            pointsJson = "4.230,103.420;4.770,103.420;5.330,103.140",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-R3-03",
            name = "Federal Route 3 - Kuala Terengganu to Kota Bharu",
            code = "Route 3",
            state = "Kelantan",
            city = "Kota Bharu",
            lengthKm = 155.0,
            pointsJson = "5.330,103.140;5.750,102.500;6.125,102.245",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),

        // === EAST MALAYSIA (SABAH & SARAWAK) ===
        RoadSegment(
            id = "MY-PBH-01",
            name = "Pan Borneo Highway - Kuching to Serian",
            code = "Route 1",
            state = "Sarawak",
            city = "Kuching",
            lengthKm = 65.0,
            pointsJson = "1.555,110.345;1.380,110.450;1.170,110.570",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        ),
        RoadSegment(
            id = "MY-PBH-02",
            name = "Pan Borneo Highway - Kota Kinabalu to Tuaran Coastal",
            code = "Route 1",
            state = "Sabah",
            city = "Kota Kinabalu",
            lengthKm = 34.0,
            pointsJson = "5.980,116.075;6.080,116.140;6.180,116.230",
            isExplored = false,
            exploredAt = null,
            timesDriven = 0
        )
    )

    val MALAYSIAN_STATES = listOf(
        "Selangor",
        "Kuala Lumpur",
        "Penang",
        "Perak",
        "Johor",
        "Melaka",
        "Negeri Sembilan",
        "Pahang",
        "Kedah",
        "Kelantan",
        "Terengganu",
        "Perlis",
        "Sabah",
        "Sarawak",
        "Putrajaya"
    )

    val STATE_CENTERS = mapOf(
        "Selangor" to GeoPoint(3.0738, 101.5183),
        "Kuala Lumpur" to GeoPoint(3.1390, 101.6869),
        "Penang" to GeoPoint(5.4141, 100.3288),
        "Perak" to GeoPoint(4.5921, 101.0901),
        "Johor" to GeoPoint(1.4854, 103.7618),
        "Melaka" to GeoPoint(2.1896, 102.2501),
        "Negeri Sembilan" to GeoPoint(2.7258, 101.9424),
        "Pahang" to GeoPoint(3.8126, 103.3256),
        "Kedah" to GeoPoint(6.1248, 100.3678),
        "Kelantan" to GeoPoint(6.1254, 102.2381),
        "Terengganu" to GeoPoint(5.3117, 103.1324),
        "Sabah" to GeoPoint(5.9804, 116.0735),
        "Sarawak" to GeoPoint(1.5533, 110.3592)
    )
}
