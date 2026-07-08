package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.database.dao.BadgeDao
import com.V2Skydivejump.app.database.entities.BadgeCriteriaType
import com.V2Skydivejump.app.database.entities.BadgeEntity
import com.V2Skydivejump.app.database.entities.JumpLogEntity
import com.V2Skydivejump.app.database.entities.UserBadgeEntity

object BadgeEvaluationEngine {

    val MILSTONE_BADGES = listOf(
        BadgeEntity("ms_500", "JUMP_MILESTONES", "Bronze Wings", "Achieved 500 jumps.", BadgeCriteriaType.AUTO_JUMPS, 500, 1, 100),
        BadgeEntity("ms_750", "JUMP_MILESTONES", "Silver Wings", "Achieved 750 jumps.", BadgeCriteriaType.AUTO_JUMPS, 750, 2, 200),
        BadgeEntity("ms_1000", "JUMP_MILESTONES", "Gold Wings", "Achieved 1,000 jumps.", BadgeCriteriaType.AUTO_JUMPS, 1000, 3, 300),
        BadgeEntity("ms_2000", "JUMP_MILESTONES", "Platinum Wings", "Achieved 2,000 jumps.", BadgeCriteriaType.AUTO_JUMPS, 2000, 4, 400),
        BadgeEntity("ms_3000", "JUMP_MILESTONES", "Diamond Wings", "Achieved 3,000 jumps.", BadgeCriteriaType.AUTO_JUMPS, 3000, 5, 500),
        BadgeEntity("ms_4000", "JUMP_MILESTONES", "Master Skydiver Wings", "Achieved 4,000 jumps.", BadgeCriteriaType.AUTO_JUMPS, 4000, 6, 600),
        BadgeEntity("ms_5000", "JUMP_MILESTONES", "Elite Skydiver Wings", "Achieved 5,000 jumps.", BadgeCriteriaType.AUTO_JUMPS, 5000, 7, 700),
        BadgeEntity("ms_7500", "JUMP_MILESTONES", "Legend Wings", "Achieved 7,500 jumps.", BadgeCriteriaType.AUTO_JUMPS, 7500, 8, 800),
        BadgeEntity("ms_10000", "JUMP_MILESTONES", "Hall of Fame Wings", "Achieved 10,000 jumps.", BadgeCriteriaType.AUTO_JUMPS, 10000, 9, 1000)
    )

    val FS_BADGES = listOf(
        BadgeEntity("fs_skydiver", "FORMATION", "Formation Skydiver", "10 FS jumps & 4-way success.", BadgeCriteriaType.AUTO_JUMPS, 10, 1, 150),
        BadgeEntity("fs_advanced", "FORMATION", "Advanced Formation Skydiver", "50 FS jumps & 8-way success.", BadgeCriteriaType.AUTO_JUMPS, 50, 2, 250),
        BadgeEntity("fs_team", "FORMATION", "Formation Team Member", "100 FS jumps & Team membership.", BadgeCriteriaType.AUTO_JUMPS, 100, 3, 350),
        BadgeEntity("fs_specialist", "FORMATION", "Formation Specialist", "250 FS jumps & advanced slot skills.", BadgeCriteriaType.AUTO_JUMPS, 250, 4, 450),
        BadgeEntity("fs_8way", "FORMATION", "8-Way Formation Member", "Participated in an 8-way or larger.", BadgeCriteriaType.AUTO_JUMPS, 1, 5, 400),
        BadgeEntity("fs_large", "FORMATION", "Large Formation Participant", "16-way or larger participation.", BadgeCriteriaType.AUTO_JUMPS, 1, 6, 550),
        BadgeEntity("fs_leader", "FORMATION", "Formation Leader", "Dive leading and organizing.", BadgeCriteriaType.MANUAL, 0, 7, 650),
        BadgeEntity("fs_national", "FORMATION", "National Formation Record Holder", "Recognized National Record.", BadgeCriteriaType.VERIFIED, 0, 8, 850),
        BadgeEntity("fs_world", "FORMATION", "World Formation Record Holder", "Recognized World Record.", BadgeCriteriaType.VERIFIED, 0, 9, 1200)
    )

    val TRAVEL_BADGES = listOf(
        BadgeEntity("tr_local", "TRAVEL", "Local Explorer", "Visit 3 unique dropzones.", BadgeCriteriaType.AUTO_DZS, 3, 1, 120),
        BadgeEntity("tr_regional", "TRAVEL", "Regional Explorer", "Visit 10 unique dropzones.", BadgeCriteriaType.AUTO_DZS, 10, 2, 220),
        BadgeEntity("tr_national", "TRAVEL", "National Explorer", "Visit 25 unique dropzones.", BadgeCriteriaType.AUTO_DZS, 25, 3, 320),
        BadgeEntity("tr_intl", "TRAVEL", "International Skydiver", "Jumps in 2 different countries.", BadgeCriteriaType.AUTO_COUNTRIES, 2, 4, 420),
        BadgeEntity("tr_world", "TRAVEL", "World Traveler", "Jumps in 5 different countries.", BadgeCriteriaType.AUTO_COUNTRIES, 5, 5, 520),
        BadgeEntity("tr_cont", "TRAVEL", "Continental Explorer", "Jumps on 2 different continents.", BadgeCriteriaType.AUTO_CONTINENTS, 2, 6, 620),
        BadgeEntity("tr_global", "TRAVEL", "Global Skydiver", "Jumps in 10 different countries.", BadgeCriteriaType.AUTO_COUNTRIES, 10, 7, 720),
        BadgeEntity("tr_boogie", "TRAVEL", "Destination Boogie Veteran", "Attend 10 recognized boogies/festivals.", BadgeCriteriaType.MANUAL, 10, 8, 820),
        BadgeEntity("tr_collector", "TRAVEL", "Aircraft Collector", "Jumps from 10 different aircraft types.", BadgeCriteriaType.AUTO_AIRCRAFTS, 10, 9, 920),
        BadgeEntity("tr_ambassador", "TRAVEL", "Dropzone Ambassador", "Visit 50 unique dropzones.", BadgeCriteriaType.AUTO_DZS, 50, 10, 1020),
        BadgeEntity("tr_explorer", "TRAVEL", "Global Explorer", "Jumps in 25 countries.", BadgeCriteriaType.AUTO_COUNTRIES, 25, 11, 1120),
        BadgeEntity("tr_world_amb", "TRAVEL", "World Skydiving Ambassador", "5 continents and 75 dropzones.", BadgeCriteriaType.VERIFIED, 0, 12, 1320),
        BadgeEntity("tr_legend", "TRAVEL", "Legend of the Skies", "50 countries and 100 dropzones.", BadgeCriteriaType.VERIFIED, 0, 13, 1600)
    )

    val SPECIAL_TRAVEL_ACHIEVEMENTS = listOf(
        BadgeEntity("sp_island", "SPECIAL", "Island Dropzone Explorer", "Jumped on an island DZ.", BadgeCriteriaType.MANUAL, 0, 1, 300),
        BadgeEntity("sp_mountain", "SPECIAL", "Mountain Dropzone Explorer", "Jumped in a mountain range.", BadgeCriteriaType.MANUAL, 0, 2, 300),
        BadgeEntity("sp_desert", "SPECIAL", "Desert Dropzone Explorer", "Jumped in a desert environment.", BadgeCriteriaType.MANUAL, 0, 3, 300),
        BadgeEntity("sp_beach", "SPECIAL", "Beach Dropzone Explorer", "Landed on a beach.", BadgeCriteriaType.MANUAL, 0, 4, 300),
        BadgeEntity("sp_arctic", "SPECIAL", "Arctic Jump Explorer", "Jumps in arctic conditions.", BadgeCriteriaType.MANUAL, 0, 5, 500),
        BadgeEntity("sp_balloon", "SPECIAL", "Balloon Jump Traveler", "Jumped from a hot air balloon.", BadgeCriteriaType.AUTO_AIRCRAFTS, 0, 6, 400),
        BadgeEntity("sp_heli", "SPECIAL", "Helicopter Jump Traveler", "Jumped from a helicopter.", BadgeCriteriaType.AUTO_AIRCRAFTS, 0, 7, 400)
    )

    val MILITARY_BADGES = listOf(
        BadgeEntity("mil_basic", "MILITARY", "Basic Parachutist", "Complete military parachutist course.", BadgeCriteriaType.VERIFIED, 0, 1, 300),
        BadgeEntity("mil_senior", "MILITARY", "Senior Parachutist", "Meet senior qualification standards.", BadgeCriteriaType.VERIFIED, 0, 2, 500),
        BadgeEntity("mil_master", "MILITARY", "Master Parachutist", "Meet master qualification standards.", BadgeCriteriaType.VERIFIED, 0, 3, 800),
        BadgeEntity("mil_sl_jm", "MILITARY", "Static Line Jumpmaster", "Qualified SL Jumpmaster duties.", BadgeCriteriaType.VERIFIED, 0, 4, 900),
        BadgeEntity("mil_mff_basic", "MILITARY", "Military Freefall Basic", "Complete Military Freefall Course.", BadgeCriteriaType.VERIFIED, 0, 5, 1000),
        BadgeEntity("mil_mff_jm", "MILITARY", "Military Freefall Jumpmaster", "Qualified MFF Jumpmaster duties.", BadgeCriteriaType.VERIFIED, 0, 6, 1200),
        BadgeEntity("mil_halo", "MILITARY", "HALO Qualified", "Conduct High Altitude Low Opening.", BadgeCriteriaType.VERIFIED, 0, 7, 1100),
        BadgeEntity("mil_haho", "MILITARY", "HAHO Qualified", "Conduct High Altitude High Opening.", BadgeCriteriaType.VERIFIED, 0, 8, 1100),
        BadgeEntity("mil_combat", "MILITARY", "Combat Parachutist", "Operational combat parachute insertion.", BadgeCriteriaType.VERIFIED, 0, 9, 2000),
        BadgeEntity("mil_pathfinder", "MILITARY", "Pathfinder", "Complete Pathfinder Course.", BadgeCriteriaType.VERIFIED, 0, 10, 1500),
        BadgeEntity("mil_ops", "MILITARY", "Airborne Operations Specialist", "Extensive airborne operational experience.", BadgeCriteriaType.VERIFIED, 0, 11, 1400),
        BadgeEntity("mil_spec_ops", "MILITARY", "Special Operations Parachutist", "Qualified MFF operator in Spec Ops.", BadgeCriteriaType.VERIFIED, 0, 12, 1800),
        BadgeEntity("mil_dist", "MILITARY", "Distinguished Military Parachutist", "Exceptional airborne career/contribution.", BadgeCriteriaType.VERIFIED, 0, 13, 2500),
        BadgeEntity("mil_honor", "MILITARY", "Airborne Hall of Honor", "Lifetime achievement in military parachuting.", BadgeCriteriaType.VERIFIED, 0, 14, 5000)
    )

    val SPECIAL_MILITARY_RECORDS = listOf(
        BadgeEntity("mil_rec_combat", "SPECIAL_MILITARY", "Combat Jump Veteran", "Operational combat jump conducted.", BadgeCriteriaType.VERIFIED, 0, 1, 1000),
        BadgeEntity("mil_rec_multiple", "SPECIAL_MILITARY", "Multiple Combat Jumps", "Two or more operational combat jumps.", BadgeCriteriaType.VERIFIED, 0, 2, 1500),
        BadgeEntity("mil_rec_pathfinder", "SPECIAL_MILITARY", "Pathfinder Graduate", "Successfully completed Pathfinder Course.", BadgeCriteriaType.VERIFIED, 0, 3, 800),
        BadgeEntity("mil_rec_instr", "SPECIAL_MILITARY", "Airborne Instructor", "Qualified military airborne instructor.", BadgeCriteriaType.VERIFIED, 0, 4, 700),
        BadgeEntity("mil_rec_mff_instr", "SPECIAL_MILITARY", "Military Freefall Instructor", "Qualified military freefall instructor.", BadgeCriteriaType.VERIFIED, 0, 5, 900),
        BadgeEntity("mil_rec_mff_ex", "SPECIAL_MILITARY", "Military Freefall Examiner", "Qualified military freefall examiner.", BadgeCriteriaType.VERIFIED, 0, 6, 1100),
        BadgeEntity("mil_rec_safety", "SPECIAL_MILITARY", "Airborne Safety Officer", "Qualified airborne safety professional.", BadgeCriteriaType.VERIFIED, 0, 7, 600),
        BadgeEntity("mil_rec_planner", "SPECIAL_MILITARY", "Airborne Operations Planner", "Expert in airborne mission planning.", BadgeCriteriaType.VERIFIED, 0, 8, 800),
        BadgeEntity("mil_rec_spec_vet", "SPECIAL_MILITARY", "Special Operations Veteran", "Served as a qualified operator in Spec Ops.", BadgeCriteriaType.VERIFIED, 0, 9, 1200),
        BadgeEntity("mil_rec_joint", "SPECIAL_MILITARY", "Joint Airborne Ops Participant", "Participated in multi-national airborne ops.", BadgeCriteriaType.VERIFIED, 0, 10, 1000)
    )

    val WINGSUIT_BADGES = listOf(
        BadgeEntity("ws_ff", "WINGSUIT", "Wingsuit First Flight", "Complete FFC and first wingsuit skydive.", BadgeCriteriaType.AUTO_JUMPS, 1, 1, 100),
        BadgeEntity("ws_pilot", "WINGSUIT", "Wingsuit Pilot", "25 wingsuit jumps logged.", BadgeCriteriaType.AUTO_JUMPS, 25, 2, 200),
        BadgeEntity("ws_advanced", "WINGSUIT", "Advanced Wingsuit Pilot", "100 wingsuit jumps logged.", BadgeCriteriaType.AUTO_JUMPS, 100, 3, 350),
        BadgeEntity("ws_formation", "WINGSUIT", "Wingsuit Formation Pilot", "200 wingsuit jumps & formation success.", BadgeCriteriaType.AUTO_JUMPS, 200, 4, 450),
        BadgeEntity("ws_performance", "WINGSUIT", "Wingsuit Performance Pilot", "300 wingsuit jumps & performance flights.", BadgeCriteriaType.AUTO_JUMPS, 300, 5, 550),
        BadgeEntity("ws_organizer", "WINGSUIT", "Wingsuit Organizer", "Lead and organize wingsuit groups.", BadgeCriteriaType.MANUAL, 0, 6, 650),
        BadgeEntity("ws_coach", "WINGSUIT", "Wingsuit Coach", "Qualified wingsuit instructor.", BadgeCriteriaType.VERIFIED, 0, 7, 800),
        BadgeEntity("ws_nat_comp", "WINGSUIT", "National Wingsuit Competitor", "Official National Competition entry.", BadgeCriteriaType.MANUAL, 0, 8, 900),
        BadgeEntity("ws_nat_champ", "WINGSUIT", "National Wingsuit Champion", "National Championship winner.", BadgeCriteriaType.MANUAL, 0, 9, 1200),
        BadgeEntity("ws_nat_rec", "WINGSUIT", "National Wingsuit Record Holder", "Recognized National Record.", BadgeCriteriaType.VERIFIED, 0, 10, 1300),
        BadgeEntity("ws_world_comp", "WINGSUIT", "World Wingsuit Competitor", "World level competition entry.", BadgeCriteriaType.MANUAL, 0, 11, 1400),
        BadgeEntity("ws_world_med", "WINGSUIT", "World Wingsuit Medalist", "World level competition medalist.", BadgeCriteriaType.MANUAL, 0, 12, 1700),
        BadgeEntity("ws_world_rec", "WINGSUIT", "World Wingsuit Record Holder", "Recognized World Record.", BadgeCriteriaType.VERIFIED, 0, 13, 2000),
        BadgeEntity("ws_world_champ", "WINGSUIT", "World Wingsuit Champion", "World Championship winner.", BadgeCriteriaType.MANUAL, 0, 14, 3000)
    )

    val FREEFLY_BADGES = listOf(
        BadgeEntity("ff_initiate", "FREEFLY", "Freefly Initiate", "Complete FFJC and 10 documented freefly jumps.", BadgeCriteriaType.AUTO_JUMPS, 10, 1, 150),
        BadgeEntity("ff_sit", "FREEFLY", "Sit-Fly Pilot", "25 freefly jumps & stable sit-fly position.", BadgeCriteriaType.AUTO_JUMPS, 25, 2, 250),
        BadgeEntity("ff_headup", "FREEFLY", "Head-Up Specialist", "50 freefly jumps & controlled head-up movement.", BadgeCriteriaType.AUTO_JUMPS, 50, 3, 350),
        BadgeEntity("ff_headdown", "FREEFLY", "Head-Down Flyer", "100 freefly jumps & stable head-down flight.", BadgeCriteriaType.AUTO_JUMPS, 100, 4, 450),
        BadgeEntity("ff_advanced", "FREEFLY", "Advanced Freefly Pilot", "200 freefly jumps & orientation transitions.", BadgeCriteriaType.AUTO_JUMPS, 200, 5, 550),
        BadgeEntity("ff_vfs", "FREEFLY", "Vertical Formation Skydiver", "300 freefly jumps & VFS participation.", BadgeCriteriaType.AUTO_JUMPS, 300, 6, 650),
        BadgeEntity("ff_organizer", "FREEFLY", "Freefly Organizer", "Organize and lead multi-way freefly jumps.", BadgeCriteriaType.MANUAL, 0, 7, 750),
        BadgeEntity("ff_nat_comp", "FREEFLY", "National Freefly Competitor", "Official National Competition entry.", BadgeCriteriaType.MANUAL, 0, 8, 850),
        BadgeEntity("ff_nat_champ", "FREEFLY", "National Freefly Champion", "National Championship winner.", BadgeCriteriaType.MANUAL, 0, 9, 1100),
        BadgeEntity("ff_world_med", "FREEFLY", "World Freefly Medalist", "World level competition medalist.", BadgeCriteriaType.MANUAL, 0, 10, 1500),
        BadgeEntity("ff_world_champ", "FREEFLY", "World Freefly Champion", "World Championship winner.", BadgeCriteriaType.MANUAL, 0, 11, 2500)
    )

    val CANOPY_BADGES = listOf(
        BadgeEntity("cp_pilot", "CANOPY_PILOTING", "Canopy Pilot", "Complete a basic canopy course.", BadgeCriteriaType.AUTO_JUMPS, 10, 1, 150),
        BadgeEntity("cp_accuracy", "CANOPY_PILOTING", "Accuracy Landing Pilot", "25 documented accuracy landings.", BadgeCriteriaType.AUTO_JUMPS, 25, 2, 250),
        BadgeEntity("cp_advanced", "CANOPY_PILOTING", "Advanced Canopy Pilot", "100 canopy-focused jumps logged.", BadgeCriteriaType.AUTO_JUMPS, 100, 3, 350),
        BadgeEntity("cp_precision", "CANOPY_PILOTING", "Precision Landing Specialist", "250 canopy-focused jumps logged.", BadgeCriteriaType.AUTO_JUMPS, 250, 4, 450),
        BadgeEntity("cp_hp", "CANOPY_PILOTING", "High Performance Canopy Pilot", "500 canopy-focused jumps logged.", BadgeCriteriaType.AUTO_JUMPS, 500, 5, 550),
        BadgeEntity("cp_swoop", "CANOPY_PILOTING", "Swoop Pilot", "Advanced canopy training & swoop intro.", BadgeCriteriaType.MANUAL, 0, 6, 650),
        BadgeEntity("cp_swoop_expert", "CANOPY_PILOTING", "Expert Swoop Pilot", "200 documented swoop landings.", BadgeCriteriaType.AUTO_JUMPS, 200, 7, 750),
        BadgeEntity("cp_coach", "CANOPY_PILOTING", "Canopy Coach", "Hold recognized coaching rating.", BadgeCriteriaType.VERIFIED, 0, 8, 900),
        BadgeEntity("cp_organizer", "CANOPY_PILOTING", "Canopy Organizer", "Lead canopy progression programs.", BadgeCriteriaType.MANUAL, 0, 9, 1000),
        BadgeEntity("cp_nat_comp", "CANOPY_PILOTING", "National Canopy Competitor", "Official National Competition entry.", BadgeCriteriaType.MANUAL, 0, 10, 1100),
        BadgeEntity("cp_nat_champ", "CANOPY_PILOTING", "National Canopy Champion", "National Championship winner.", BadgeCriteriaType.MANUAL, 0, 11, 1300),
        BadgeEntity("cp_nat_rec", "CANOPY_PILOTING", "National Canopy Record Holder", "Recognized National Record.", BadgeCriteriaType.VERIFIED, 0, 12, 1400),
        BadgeEntity("cp_world_comp", "CANOPY_PILOTING", "World Canopy Competitor", "World Competition entry.", BadgeCriteriaType.MANUAL, 0, 13, 1500),
        BadgeEntity("cp_world_med", "CANOPY_PILOTING", "World Canopy Medalist", "World Competition medalist.", BadgeCriteriaType.MANUAL, 0, 14, 1800),
        BadgeEntity("cp_world_rec", "CANOPY_PILOTING", "World Canopy Record Holder", "Recognized World Record.", BadgeCriteriaType.VERIFIED, 0, 15, 2000),
        BadgeEntity("cp_world_champ", "CANOPY_PILOTING", "World Canopy Champion", "World Championship winner.", BadgeCriteriaType.MANUAL, 0, 16, 3000)
    )

    val SAFETY_BADGES = listOf(
        BadgeEntity("sf_aware", "SAFETY", "Safety Aware Skydiver", "Complete Safety & Emergency Training.", BadgeCriteriaType.VERIFIED, 0, 1, 200),
        BadgeEntity("sf_commit", "SAFETY", "Safety Commitment Award", "100 incident-free jumps logged.", BadgeCriteriaType.AUTO_JUMPS, 100, 2, 300),
        BadgeEntity("sf_excellent", "SAFETY", "Safety Excellence Award", "500 incident-free jumps logged.", BadgeCriteriaType.AUTO_JUMPS, 500, 3, 500),
        BadgeEntity("sf_risk", "SAFETY", "Risk Management Specialist", "1,000 incident-free jumps & briefing participation.", BadgeCriteriaType.AUTO_JUMPS, 1000, 4, 700),
        BadgeEntity("sf_mentor", "SAFETY", "Safety Mentor", "Mentor newer jumpers & assist in safety education.", BadgeCriteriaType.VERIFIED, 0, 5, 800),
        BadgeEntity("sf_leader", "SAFETY", "Safety Leader", "Leadership in promoting safety culture.", BadgeCriteriaType.VERIFIED, 0, 6, 1000),
        BadgeEntity("sf_2500", "SAFETY", "Incident-Free 2,500", "2,500 consecutive incident-free jumps.", BadgeCriteriaType.AUTO_JUMPS, 2500, 7, 1500),
        BadgeEntity("sf_5000", "SAFETY", "Incident-Free 5,000", "5,000 consecutive incident-free jumps.", BadgeCriteriaType.AUTO_JUMPS, 5000, 8, 2000),
        BadgeEntity("sf_dist", "SAFETY", "Distinguished Safety Professional", "Long-term contribution to safety programs.", BadgeCriteriaType.VERIFIED, 0, 9, 2500),
        BadgeEntity("sf_national", "SAFETY", "National Safety Award", "Official recognition by national organization.", BadgeCriteriaType.VERIFIED, 0, 10, 3500),
        BadgeEntity("sf_lifetime", "SAFETY", "Lifetime Safety Achievement", "Exceptional lifetime contribution to safety.", BadgeCriteriaType.VERIFIED, 0, 11, 6000)
    )

    val SPECIAL_ACHIEVEMENTS = listOf(
        BadgeEntity("sa_hof", "SPECIAL_ACHIEVEMENT", "Hall of Fame Induction", "Lifetime contribution to skydiving & exceptional achievements.", BadgeCriteriaType.VERIFIED, 0, 12, 10000),
        BadgeEntity("sa_lifetime", "SPECIAL_ACHIEVEMENT", "Lifetime Achievement Award", "Long-term leadership & sustained excellence.", BadgeCriteriaType.VERIFIED, 0, 11, 8000),
        BadgeEntity("sa_world_rec", "SPECIAL_ACHIEVEMENT", "World Record Holder", "Verified participation in recognized world record.", BadgeCriteriaType.VERIFIED, 0, 10, 5000),
        BadgeEntity("sa_nat_rec", "SPECIAL_ACHIEVEMENT", "National Record Holder", "Verified participation in national record.", BadgeCriteriaType.VERIFIED, 0, 9, 3000),
        BadgeEntity("sa_safety", "SPECIAL_ACHIEVEMENT", "Exceptional Safety Contribution Award", "Major contribution to safety improvements.", BadgeCriteriaType.VERIFIED, 0, 8, 2000),
        BadgeEntity("sa_service", "SPECIAL_ACHIEVEMENT", "Distinguished Service Award", "Outstanding service to community/orgs.", BadgeCriteriaType.VERIFIED, 0, 7, 1500),
        BadgeEntity("sa_first", "SPECIAL_ACHIEVEMENT", "First-of-Type Achievement", "First successful rare/unique jump type.", BadgeCriteriaType.VERIFIED, 0, 6, 1200),
        BadgeEntity("sa_innovation", "SPECIAL_ACHIEVEMENT", "Innovation in Skydiving Award", "Innovation in gear, training, or ops.", BadgeCriteriaType.VERIFIED, 0, 5, 1000),
        BadgeEntity("sa_extreme", "SPECIAL_ACHIEVEMENT", "Extreme Environment Jump Achievement", "Jumps in extreme conditions.", BadgeCriteriaType.VERIFIED, 0, 4, 800),
        BadgeEntity("sa_demo", "SPECIAL_ACHIEVEMENT", "Special Operations Demonstration Award", "Participation in experimental or demonstration jumps.", BadgeCriteriaType.VERIFIED, 0, 3, 600),
        BadgeEntity("sa_milestone", "SPECIAL_ACHIEVEMENT", "Milestone Firsts", "Recognition of important personal firsts.", BadgeCriteriaType.VERIFIED, 0, 2, 400),
        BadgeEntity("sa_dz_rec", "SPECIAL_ACHIEVEMENT", "Dropzone Special Recognition Award", "Recognition by specific dropzone management.", BadgeCriteriaType.VERIFIED, 0, 1, 300)
    )

    val AMBASSADOR_BADGES = listOf(
        BadgeEntity("amb_bronze", "AMBASSADOR", "Bronze Ambassador", "5 successful referrals.", BadgeCriteriaType.VERIFIED, 5, 1, 500),
        BadgeEntity("amb_silver", "AMBASSADOR", "Silver Ambassador", "15 successful referrals.", BadgeCriteriaType.VERIFIED, 15, 2, 1500),
        BadgeEntity("amb_gold", "AMBASSADOR", "Gold Ambassador", "30 successful referrals.", BadgeCriteriaType.VERIFIED, 30, 3, 3000),
        BadgeEntity("amb_platinum", "AMBASSADOR", "Platinum Ambassador", "50 successful referrals.", BadgeCriteriaType.VERIFIED, 50, 4, 5000),
        BadgeEntity("amb_diamond", "AMBASSADOR", "Diamond Ambassador", "100 successful referrals.", BadgeCriteriaType.VERIFIED, 100, 5, 10000)
    )

    val DZO_RECRUITER_BADGES = listOf(
        BadgeEntity("dzo_scout", "DZO_RECRUITMENT", "DZO Scout", "Refer 1 verified DZO.", BadgeCriteriaType.VERIFIED, 1, 1, 1000),
        BadgeEntity("dzo_builder", "DZO_RECRUITMENT", "DZO Builder", "Refer 3 verified DZOs.", BadgeCriteriaType.VERIFIED, 3, 2, 3000),
        BadgeEntity("dzo_networker", "DZO_RECRUITMENT", "DZO Networker", "Refer 5 verified DZOs.", BadgeCriteriaType.VERIFIED, 5, 3, 5000),
        BadgeEntity("dzo_pioneer", "DZO_RECRUITMENT", "DZO Pioneer", "Refer 10 verified DZOs.", BadgeCriteriaType.VERIFIED, 10, 4, 10000),
        BadgeEntity("dzo_global", "DZO_RECRUITMENT", "Global Connector", "Refer 25 verified DZOs.", BadgeCriteriaType.VERIFIED, 25, 5, 25000)
    )

    val COMMUNITY_BUILDER_BADGES = listOf(
        BadgeEntity("cb_builder", "COMMUNITY", "Community Builder", "25 total verified referrals.", BadgeCriteriaType.VERIFIED, 25, 1, 2500),
        BadgeEntity("cb_leader", "COMMUNITY", "Community Leader", "50 total verified referrals.", BadgeCriteriaType.VERIFIED, 50, 2, 5000),
        BadgeEntity("cb_champion", "COMMUNITY", "Community Champion", "100 total verified referrals.", BadgeCriteriaType.VERIFIED, 100, 3, 10000),
        BadgeEntity("cb_legend", "COMMUNITY", "Community Legend", "250 total verified referrals.", BadgeCriteriaType.VERIFIED, 250, 4, 25000)
    )

    suspend fun evaluateAutoBadges(
        userId: String,
        jumpLogs: List<JumpLogEntity>,
        badgeDao: BadgeDao
    ) {
        val totalJumps = jumpLogs.size
        val now = TimeUtils.nowEpochMillis()

        // Evaluate Milestones
        MILSTONE_BADGES.forEach { badge ->
            if (totalJumps >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // Evaluate Safety (based on incident-free jump count)
        SAFETY_BADGES.filter { it.criteriaType == BadgeCriteriaType.AUTO_JUMPS }.forEach { badge ->
            if (totalJumps >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // Evaluate Formation (FS/RW)
        val fsJumps = jumpLogs.count { 
            it.disciplines?.contains("Formation", ignoreCase = true) == true || 
            it.disciplines?.contains("Belly", ignoreCase = true) == true ||
            it.disciplines?.contains("-Way") == true
        }
        FS_BADGES.filter { it.criteriaType == BadgeCriteriaType.AUTO_JUMPS }.forEach { badge ->
            when (badge.id) {
                "fs_skydiver" -> {
                    val fourWayPlus = jumpLogs.count { it.disciplines?.contains("4-Way") == true || it.disciplines?.contains("4+ Way") == true || it.disciplines?.contains("8-Way") == true || it.disciplines?.contains("8+ Way") == true || it.disciplines?.contains("16-Way") == true }
                    if (fsJumps >= 10 && fourWayPlus >= 1) awardIfMissing(userId, badge.id, badgeDao, now)
                }
                "fs_advanced" -> {
                    val eightWayPlus = jumpLogs.count { it.disciplines?.contains("8-Way") == true || it.disciplines?.contains("8+ Way") == true || it.disciplines?.contains("16-Way") == true }
                    if (fsJumps >= 50 && eightWayPlus >= 1) awardIfMissing(userId, badge.id, badgeDao, now)
                }
                "fs_8way" -> {
                    if (jumpLogs.any { it.disciplines?.contains("8-Way") == true || it.disciplines?.contains("8+ Way") == true || it.disciplines?.contains("16-Way") == true }) {
                        awardIfMissing(userId, badge.id, badgeDao, now)
                    }
                }
                "fs_large" -> {
                    if (jumpLogs.any { it.disciplines?.contains("16-Way") == true }) {
                        awardIfMissing(userId, badge.id, badgeDao, now)
                    }
                }
                else -> {
                    if (fsJumps >= badge.criteriaValue) {
                        awardIfMissing(userId, badge.id, badgeDao, now)
                    }
                }
            }
        }

        // Evaluate Wingsuit
        val wsJumps = jumpLogs.count { it.disciplines?.contains("Wingsuit", ignoreCase = true) == true }
        WINGSUIT_BADGES.filter { it.criteriaType == BadgeCriteriaType.AUTO_JUMPS }.forEach { badge ->
            if (wsJumps >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // Evaluate Freefly
        val ffJumps = jumpLogs.count { 
            it.disciplines?.contains("Freefly", ignoreCase = true) == true || 
            it.disciplines?.contains("Sit Fly", ignoreCase = true) == true ||
            it.disciplines?.contains("Head-Up", ignoreCase = true) == true ||
            it.disciplines?.contains("Head-Down", ignoreCase = true) == true ||
            it.disciplines?.contains("VFS", ignoreCase = true) == true
        }
        FREEFLY_BADGES.filter { it.criteriaType == BadgeCriteriaType.AUTO_JUMPS }.forEach { badge ->
            if (ffJumps >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // Evaluate Canopy Piloting
        val cpJumps = jumpLogs.count { 
            it.disciplines?.contains("Canopy", ignoreCase = true) == true || 
            it.disciplines?.contains("Accuracy", ignoreCase = true) == true || 
            it.landingStyles?.contains("Accuracy", ignoreCase = true) == true ||
            it.landingStyles?.contains("Swoop", ignoreCase = true) == true
        }
        CANOPY_BADGES.filter { it.criteriaType == BadgeCriteriaType.AUTO_JUMPS }.forEach { badge ->
            if (cpJumps >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // Evaluate Travel
        val distinctDzs = jumpLogs.mapNotNull { it.dzName }.filter { it.isNotBlank() }.distinct().size
        val distinctCountries = jumpLogs.mapNotNull { it.country }.filter { it.isNotBlank() }.distinct().size
        val distinctAircrafts = jumpLogs.mapNotNull { it.aircraftType }.filter { it.isNotBlank() }.distinct().size

        TRAVEL_BADGES.forEach { badge ->
            when (badge.criteriaType) {
                BadgeCriteriaType.AUTO_DZS -> if (distinctDzs >= badge.criteriaValue) awardIfMissing(userId, badge.id, badgeDao, now)
                BadgeCriteriaType.AUTO_COUNTRIES -> if (distinctCountries >= badge.criteriaValue) awardIfMissing(userId, badge.id, badgeDao, now)
                BadgeCriteriaType.AUTO_AIRCRAFTS -> if (distinctAircrafts >= badge.criteriaValue) awardIfMissing(userId, badge.id, badgeDao, now)
                else -> {}
            }
        }

        // Special Aircraft checks
        if (jumpLogs.any { it.aircraftType?.contains("Balloon", ignoreCase = true) == true }) awardIfMissing(userId, "sp_balloon", badgeDao, now)
        if (jumpLogs.any { it.aircraftType?.contains("Helicopter", ignoreCase = true) == true || it.aircraftType?.contains("Heli", ignoreCase = true) == true }) awardIfMissing(userId, "sp_heli", badgeDao, now)
    }

    private suspend fun awardIfMissing(
        userId: String,
        badgeId: String,
        badgeDao: BadgeDao,
        dateEarned: Long
    ) {
        if (badgeDao.hasBadge(userId, badgeId) == null) {
            badgeDao.awardBadge(
                UserBadgeEntity(
                    userId = userId,
                    badgeId = badgeId,
                    dateEarned = dateEarned,
                    isNew = true
                )
            )
        }
    }

    suspend fun evaluateReferralBadges(
        userId: String,
        totalReferrals: Int,
        dzoReferrals: Int,
        badgeDao: BadgeDao
    ) {
        val now = TimeUtils.nowEpochMillis()

        // Skydiver Ambassador Series
        AMBASSADOR_BADGES.forEach { badge ->
            if (totalReferrals >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // DZO Recruitment Awards
        DZO_RECRUITER_BADGES.forEach { badge ->
            if (dzoReferrals >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // Community Builder Awards
        COMMUNITY_BUILDER_BADGES.forEach { badge ->
            if (totalReferrals >= badge.criteriaValue) {
                awardIfMissing(userId, badge.id, badgeDao, now)
            }
        }

        // Special Recognition
        if (totalReferrals >= 1) awardIfMissing(userId, "rec_first", badgeDao, now)
        if (totalReferrals >= 25) awardIfMissing(userId, "rec_evangelist", badgeDao, now)
        if (dzoReferrals >= 5) awardIfMissing(userId, "rec_developer", badgeDao, now)
        if (dzoReferrals >= 10 && totalReferrals >= 100) awardIfMissing(userId, "rec_architect", badgeDao, now)
    }
}
