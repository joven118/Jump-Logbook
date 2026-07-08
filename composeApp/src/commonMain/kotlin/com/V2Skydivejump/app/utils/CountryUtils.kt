package com.V2Skydivejump.app.utils

object CountryUtils {
    val countries = listOf(
        // Africa
        "Algeria", "Angola", "Benin", "Botswana", "Burkina Faso", "Burundi", "Cabo Verde", "Cameroon",
        "Central African Republic", "Chad", "Comoros", "Democratic Republic of the Congo", "Republic of the Congo",
        "Djibouti", "Egypt", "Equatorial Guinea", "Eritrea", "Eswatini", "Ethiopia", "Gabon", "Gambia",
        "Ghana", "Guinea", "Guinea-Bissau", "Côte d'Ivoire", "Kenya", "Lesotho", "Liberia", "Libya",
        "Madagascar", "Malawi", "Mali", "Mauritania", "Mauritius", "Morocco", "Mozambique", "Namibia",
        "Niger", "Nigeria", "Rwanda", "São Tomé and Príncipe", "Senegal", "Seychelles", "Sierra Leone",
        "Somalia", "South Africa", "South Sudan", "Sudan", "Tanzania", "Togo", "Tunisia", "Uganda",
        "Zambia", "Zimbabwe",
        // Asia
        "Afghanistan", "Armenia", "Azerbaijan", "Bahrain", "Bangladesh", "Bhutan", "Brunei", "Cambodia",
        "China", "Cyprus", "Georgia", "India", "Indonesia", "Iran", "Iraq", "Israel", "Japan", "Jordan",
        "Kazakhstan", "Kuwait", "Kyrgyzstan", "Laos", "Lebanon", "Malaysia", "Maldives", "Mongolia",
        "Myanmar", "Nepal", "North Korea", "Oman", "Pakistan", "Philippines", "Qatar", "Saudi Arabia",
        "Singapore", "South Korea", "Sri Lanka", "Syria", "Tajikistan", "Thailand", "Timor-Leste", "Turkey",
        "Turkmenistan", "United Arab Emirates", "Uzbekistan", "Vietnam", "Yemen",
        // Europe
        "Albania", "Andorra", "Austria", "Belarus", "Belgium", "Bosnia and Herzegovina", "Bulgaria", "Croatia",
        "Czech Republic", "Estonia", "Finland", "France", "Germany", "Greece", "Hungary", "Iceland",
        "Ireland", "Italy", "Latvia", "Liechtenstein", "Lithuania", "Luxembourg", "Malta", "Moldova",
        "Monaco", "Montenegro", "Netherlands", "North Macedonia", "Norway", "Poland", "Portugal", "Romania",
        "Russia", "San Marino", "Serbia", "Slovakia", "Slovenia", "Spain", "Sweden", "Switzerland", "Ukraine",
        "United Kingdom", "Vatican City", "Denmark",
        // North America
        "Antigua and Barbuda", "Bahamas", "Barbados", "Belize", "Canada", "Costa Rica", "Cuba", "Dominica",
        "Dominican Republic", "El Salvador", "Grenada", "Guatemala", "Haiti", "Honduras", "Jamaica", "Mexico",
        "Nicaragua", "Panama", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines",
        "Trinidad and Tobago", "United States",
        // South America
        "Argentina", "Bolivia", "Brazil", "Chile", "Colombia", "Ecuador", "Guyana", "Paraguay", "Peru",
        "Suriname", "Uruguay", "Venezuela",
        // Oceania
        "Australia", "Fiji", "Kiribati", "Marshall Islands", "Micronesia", "Nauru", "New Zealand", "Palau",
        "Papua New Guinea", "Samoa", "Solomon Islands", "Tonga", "Tuvalu", "Vanuatu", "Palestine"
    ).sorted()

    private val countryToRegion = mapOf(
        // Africa
        "Algeria" to "Africa", "Angola" to "Africa", "Benin" to "Africa", "Botswana" to "Africa", 
        "Burkina Faso" to "Africa", "Burundi" to "Africa", "Cabo Verde" to "Africa", "Cameroon" to "Africa",
        "Central African Republic" to "Africa", "Chad" to "Africa", "Comoros" to "Africa", 
        "Democratic Republic of the Congo" to "Africa", "Republic of the Congo" to "Africa",
        "Djibouti" to "Africa", "Egypt" to "Africa", "Equatorial Guinea" to "Africa", "Eritrea" to "Africa", 
        "Eswatini" to "Africa", "Ethiopia" to "Africa", "Gabon" to "Africa", "Gambia" to "Africa",
        "Ghana" to "Africa", "Guinea" to "Africa", "Guinea-Bissau" to "Africa", "Côte d'Ivoire" to "Africa", 
        "Kenya" to "Africa", "Lesotho" to "Africa", "Liberia" to "Africa", "Libya" to "Africa",
        "Madagascar" to "Africa", "Malawi" to "Africa", "Mali" to "Africa", "Mauritania" to "Africa", 
        "Mauritius" to "Africa", "Morocco" to "Africa", "Mozambique" to "Africa", "Namibia" to "Africa",
        "Niger" to "Africa", "Nigeria" to "Africa", "Rwanda" to "Africa", "São Tomé and Príncipe" to "Africa", 
        "Senegal" to "Africa", "Seychelles" to "Africa", "Sierra Leone" to "Africa",
        "Somalia" to "Africa", "South Africa" to "Africa", "South Sudan" to "Africa", "Sudan" to "Africa", 
        "Tanzania" to "Africa", "Togo" to "Africa", "Tunisia" to "Africa", "Uganda" to "Africa",
        "Zambia" to "Africa", "Zimbabwe" to "Africa",
        // Asia
        "Afghanistan" to "Asia", "Armenia" to "Asia", "Azerbaijan" to "Asia", "Bahrain" to "Asia", 
        "Bangladesh" to "Asia", "Bhutan" to "Asia", "Brunei" to "Asia", "Cambodia" to "Asia",
        "China" to "Asia", "Cyprus" to "Asia", "Georgia" to "Asia", "India" to "Asia", 
        "Indonesia" to "Asia", "Iran" to "Asia", "Iraq" to "Asia", "Israel" to "Asia", 
        "Japan" to "Asia", "Jordan" to "Asia", "Kazakhstan" to "Asia", "Kuwait" to "Asia", 
        "Kyrgyzstan" to "Asia", "Laos" to "Asia", "Lebanon" to "Asia", "Malaysia" to "Asia", 
        "Maldives" to "Asia", "Mongolia" to "Asia", "Myanmar" to "Asia", "Nepal" to "Asia", 
        "North Korea" to "Asia", "Oman" to "Asia", "Pakistan" to "Asia", "Philippines" to "Asia", 
        "Qatar" to "Asia", "Saudi Arabia" to "Asia", "Singapore" to "Asia", "South Korea" to "Asia", 
        "Sri Lanka" to "Asia", "Syria" to "Asia", "Tajikistan" to "Asia", "Thailand" to "Asia", 
        "Timor-Leste" to "Asia", "Turkey" to "Asia", "Turkmenistan" to "Asia", 
        "United Arab Emirates" to "Asia", "Uzbekistan" to "Asia", "Vietnam" to "Asia", "Yemen" to "Asia",
        // Europe
        "Albania" to "Europe", "Andorra" to "Europe", "Austria" to "Europe", "Belarus" to "Europe", 
        "Belgium" to "Europe", "Bosnia and Herzegovina" to "Europe", "Bulgaria" to "Europe", "Croatia" to "Europe",
        "Czech Republic" to "Europe", "Estonia" to "Europe", "Finland" to "Europe", "France" to "Europe", 
        "Germany" to "Europe", "Greece" to "Europe", "Hungary" to "Europe", "Iceland" to "Europe",
        "Ireland" to "Europe", "Italy" to "Europe", "Latvia" to "Europe", "Liechtenstein" to "Europe", 
        "Lithuania" to "Europe", "Luxembourg" to "Europe", "Malta" to "Europe", "Moldova" to "Europe",
        "Monaco" to "Europe", "Montenegro" to "Europe", "Netherlands" to "Europe", "North Macedonia" to "Europe", 
        "Norway" to "Europe", "Poland" to "Europe", "Portugal" to "Europe", "Romania" to "Europe",
        "Russia" to "Europe", "San Marino" to "Europe", "Serbia" to "Europe", "Slovakia" to "Europe", 
        "Slovenia" to "Europe", "Spain" to "Europe", "Sweden" to "Europe", "Switzerland" to "Europe", 
        "Ukraine" to "Europe", "United Kingdom" to "Europe", "Vatican City" to "Europe", "Denmark" to "Europe",
        // North America
        "Antigua and Barbuda" to "North America", "Bahamas" to "North America", "Barbados" to "North America", 
        "Belize" to "North America", "Canada" to "North America", "Costa Rica" to "North America", 
        "Cuba" to "North America", "Dominica" to "North America", "Dominican Republic" to "North America", 
        "El Salvador" to "North America", "Grenada" to "North America", "Guatemala" to "North America", 
        "Haiti" to "North America", "Honduras" to "North America", "Jamaica" to "North America", "Mexico" to "North America",
        "Nicaragua" to "North America", "Panama" to "North America", "Saint Kitts and Nevis" to "North America", 
        "Saint Lucia" to "North America", "Saint Vincent and the Grenadines" to "North America",
        "Trinidad and Tobago" to "North America", "United States" to "North America",
        // South America
        "Argentina" to "South America", "Bolivia" to "South America", "Brazil" to "South America", "Chile" to "South America", 
        "Colombia" to "South America", "Ecuador" to "South America", "Guyana" to "South America", 
        "Paraguay" to "South America", "Peru" to "South America", "Suriname" to "South America", 
        "Uruguay" to "South America", "Venezuela" to "South America",
        // Oceania
        "Australia" to "Oceania", "Fiji" to "Oceania", "Kiribati" to "Oceania", "Marshall Islands" to "Oceania", 
        "Micronesia" to "Oceania", "Nauru" to "Oceania", "New Zealand" to "Oceania", "Palau" to "Oceania",
        "Papua New Guinea" to "Oceania", "Samoa" to "Oceania", "Solomon Islands" to "Oceania", "Tonga" to "Oceania", 
        "Tuvalu" to "Oceania", "Vanuatu" to "Oceania", "Palestine" to "Asia"
    )

    fun getRegion(countryName: String): String {
        return countryToRegion[countryName] ?: "Other"
    }

    fun getRegionProximityScore(userRegion: String, targetRegion: String): Int {
        if (userRegion == targetRegion) return 0
        
        // Simple proximity logic: 
        // 1. Same region (0)
        // 2. Adjacent regions (1)
        // 3. Far regions (2)
        
        val map = mapOf(
            "Asia" to listOf("Oceania", "Europe", "Africa"),
            "Europe" to listOf("Asia", "Africa", "North America"),
            "Africa" to listOf("Europe", "Asia", "South America"),
            "North America" to listOf("Europe", "South America", "Oceania"),
            "South America" to listOf("North America", "Africa"),
            "Oceania" to listOf("Asia", "North America")
        )
        
        return if (map[userRegion]?.contains(targetRegion) == true) 1 else 2
    }

    fun getFlagEmoji(countryName: String): String {
        return when (countryName.trim().lowercase()) {
            "afghanistan" -> "🇦🇫"
            "albania" -> "🇦🇱"
            "algeria" -> "🇩🇿"
            "andorra" -> "🇦🇩"
            "angola" -> "🇦🇴"
            "antigua and barbuda" -> "🇦🇬"
            "argentina" -> "🇦🇷"
            "armenia" -> "🇦🇲"
            "australia" -> "🇦🇺"
            "austria" -> "🇦🇹"
            "azerbaijan" -> "🇦🇿"
            "bahamas" -> "🇧🇸"
            "bahrain" -> "🇧🇭"
            "bangladesh" -> "🇧🇩"
            "barbados" -> "🇧🇧"
            "belarus" -> "🇧🇾"
            "belgium" -> "🇧🇪"
            "belize" -> "🇧🇿"
            "benin" -> "🇧🇯"
            "bhutan" -> "🇧🇹"
            "bolivia" -> "🇧🇴"
            "bosnia and herzegovina" -> "🇧🇦"
            "botswana" -> "🇧🇼"
            "brazil" -> "🇧🇷"
            "brunei" -> "🇧🇳"
            "bulgaria" -> "🇧🇬"
            "burkina faso" -> "🇧🇫"
            "burundi" -> "🇧🇮"
            "cabo verde" -> "🇨🇻"
            "cambodia" -> "🇰🇭"
            "cameroon" -> "🇨🇲"
            "canada" -> "🇨🇦"
            "central african republic" -> "🇨🇫"
            "chad" -> "🇹🇩"
            "chile" -> "🇨🇱"
            "china" -> "🇨🇳"
            "colombia" -> "🇨🇴"
            "comoros" -> "🇰🇲"
            "congo, democratic republic of the" -> "🇨🇩"
            "democratic republic of the congo" -> "🇨🇩"
            "congo, republic of the" -> "🇨🇬"
            "republic of the congo" -> "🇨🇬"
            "costa rica" -> "🇨🇷"
            "côte d'ivoire" -> "🇨🇮"
            "croatia" -> "🇭🇷"
            "cuba" -> "🇨🇺"
            "cyprus" -> "🇨🇾"
            "czech republic" -> "🇨🇿"
            "denmark" -> "🇩🇰"
            "djibouti" -> "🇩🇯"
            "dominica" -> "🇩🇲"
            "dominican republic" -> "🇩🇴"
            "ecuador" -> "🇪🇨"
            "egypt" -> "🇪🇬"
            "el salvador" -> "🇸🇻"
            "equatorial guinea" -> "🇬🇶"
            "eritrea" -> "🇪🇷"
            "estonia" -> "🇪🇪"
            "eswatini" -> "🇸🇿"
            "ethiopia" -> "🇪🇹"
            "fiji" -> "🇫🇯"
            "finland" -> "🇫🇮"
            "france" -> "🇫🇷"
            "gabon" -> "🇬🇦"
            "gambia" -> "🇬🇲"
            "georgia" -> "🇬🇪"
            "germany" -> "🇩🇪"
            "ghana" -> "🇬🇭"
            "greece" -> "🇬🇷"
            "grenada" -> "🇬🇩"
            "guatemala" -> "🇬🇹"
            "guinea" -> "🇬🇳"
            "guinea-bissau" -> "🇬🇼"
            "guyana" -> "🇬🇾"
            "haiti" -> "🇭🇹"
            "honduras" -> "🇭🇳"
            "hungary" -> "🇭🇺"
            "iceland" -> "🇮🇸"
            "india" -> "🇮🇳"
            "indonesia" -> "🇮🇩"
            "iran" -> "🇮🇷"
            "iraq" -> "🇮🇶"
            "ireland" -> "🇮🇪"
            "israel" -> "🇮🇱"
            "italy" -> "🇮🇹"
            "jamaica" -> "🇯🇲"
            "japan" -> "🇯🇵"
            "jordan" -> "🇯🇴"
            "kazakhstan" -> "🇰🇿"
            "kenya" -> "🇰🇪"
            "kiribati" -> "🇰🇮"
            "north korea" -> "🇰🇵"
            "south korea" -> "🇰🇷"
            "kuwait" -> "🇰🇼"
            "kyrgyzstan" -> "🇰🇬"
            "laos" -> "🇱🇦"
            "latvia" -> "🇱🇻"
            "lebanon" -> "🇱🇧"
            "lesotho" -> "🇱🇸"
            "liberia" -> "🇱🇷"
            "libya" -> "🇱🇾"
            "liechtenstein" -> "🇱🇮"
            "lithuania" -> "🇱🇹"
            "luxembourg" -> "🇱🇺"
            "madagascar" -> "🇲🇬"
            "malawi" -> "🇲🇼"
            "malaysia" -> "🇲🇾"
            "maldives" -> "🇲🇻"
            "mali" -> "🇲🇱"
            "malta" -> "🇲🇹"
            "marshall islands" -> "🇲🇭"
            "mauritania" -> "🇲🇷"
            "mauritius" -> "🇲🇺"
            "mexico" -> "🇲🇽"
            "micronesia" -> "🇫🇲"
            "moldova" -> "🇲🇩"
            "monaco" -> "🇲🇨"
            "mongolia" -> "🇲🇳"
            "montenegro" -> "🇲🇪"
            "morocco" -> "🇲🇦"
            "mozambique" -> "🇲🇿"
            "myanmar" -> "🇲🇲"
            "namibia" -> "🇳🇦"
            "nauru" -> "🇳🇷"
            "nepal" -> "🇳🇵"
            "netherlands" -> "🇳🇱"
            "new zealand" -> "🇳🇿"
            "nicaragua" -> "🇳🇮"
            "niger" -> "🇳🇪"
            "nigeria" -> "🇳🇬"
            "north macedonia" -> "🇲🇰"
            "norway" -> "🇳🇴"
            "oman" -> "🇴🇲"
            "pakistan" -> "🇵🇰"
            "palau" -> "🇵🇼"
            "palestine" -> "🇵🇸"
            "panama" -> "🇵🇦"
            "papua new guinea" -> "🇵🇬"
            "paraguay" -> "🇵🇾"
            "peru" -> "🇵🇪"
            "philippines" -> "🇵🇭"
            "poland" -> "🇵🇱"
            "portugal" -> "🇵🇹"
            "qatar" -> "🇶🇦"
            "romania" -> "🇷🇴"
            "russia" -> "🇷🇺"
            "rwanda" -> "🇷🇼"
            "saint kitts and nevis" -> "🇰🇳"
            "saint lucia" -> "🇱🇨"
            "saint vincent and the grenadines" -> "🇻🇨"
            "samoa" -> "🇼🇸"
            "san marino" -> "🇸🇲"
            "são tomé and príncipe" -> "🇸🇹"
            "saudi arabia" -> "🇸🇦"
            "senegal" -> "🇸🇳"
            "serbia" -> "🇷🇸"
            "seychelles" -> "🇸🇨"
            "sierra leone" -> "🇸🇱"
            "singapore" -> "🇸🇬"
            "slovakia" -> "🇸🇰"
            "slovenia" -> "🇸🇮"
            "solomon islands" -> "🇸🇧"
            "somalia" -> "🇸🇴"
            "south africa" -> "🇿🇦"
            "south sudan" -> "🇸🇸"
            "spain" -> "🇪🇸"
            "sri lanka" -> "🇱🇰"
            "sudan" -> "🇸🇩"
            "suriname" -> "🇸🇷"
            "sweden" -> "🇸🇪"
            "switzerland" -> "🇨🇭"
            "syria" -> "🇸🇾"
            "tajikistan" -> "🇹🇯"
            "tanzania" -> "🇹🇿"
            "thailand" -> "🇹🇭"
            "timor-leste" -> "🇹🇱"
            "togo" -> "🇹🇬"
            "tonga" -> "🇹🇴"
            "trinidad and tobago" -> "🇹🇹"
            "tunisia" -> "🇹🇳"
            "turkey" -> "🇹🇷"
            "turkmenistan" -> "🇹🇲"
            "tuvalu" -> "🇹🇻"
            "uganda" -> "🇺🇬"
            "ukraine" -> "🇺🇦"
            "united arab emirates" -> "🇦🇪"
            "united kingdom" -> "🇬🇧"
            "united states" -> "🇺🇸"
            "uruguay" -> "🇺🇾"
            "uzbekistan" -> "🇺🇿"
            "vanuatu" -> "🇻🇺"
            "vatican city" -> "🇻🇦"
            "venezuela" -> "🇻🇪"
            "vietnam" -> "🇻🇳"
            "yemen" -> "🇾🇪"
            "zambia" -> "🇿🇲"
            "zimbabwe" -> "🇿🇼"
            else -> "🏳️"
        }
    }
}
