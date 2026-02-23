package com.Placements.Ready.data

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class Company(
    @SerializedName("company_name") val companyName: String = "Unknown",
    @SerializedName("drive_month") val driveMonth: String? = null,
    @SerializedName("drive_type") val driveType: String? = null,
    @JsonAdapter(StringOrListDeserializer::class)
    @SerializedName("drive_dates") val driveDates: List<String>? = null,
    @SerializedName("total_rounds") val totalRounds: Int? = null,
    @SerializedName("round_details") val roundDetails: List<RoundDetail>? = null,
    @SerializedName("final_round_date") val finalRoundDate: String? = null,
    @SerializedName("offer_letter_date") val offerLetterDate: String? = null,
    @SerializedName("final_selected_count") val finalSelectedCount: Int? = null,
    @JsonAdapter(StringOrListDeserializer::class)
    @SerializedName("roles_offered") val rolesOffered: List<String>? = null,
    @SerializedName("package_salary") val packageSalary: PackageSalary? = null,
    @SerializedName("eligibility_cgpa") val eligibilityCgpa: String? = null,
    @SerializedName("registration_deadline") val registrationDeadline: String? = null,
    @JsonAdapter(StringOrListDeserializer::class)
    @SerializedName("skills_required") val skillsRequired: List<String>? = null,
    @SerializedName("job_location") val jobLocation: String? = null,
    @SerializedName("batch") val batch: String? = null,
    @JsonAdapter(StringOrListDeserializer::class)
    @SerializedName("selection_process_steps") val selectionProcessSteps: List<String>? = null,
    @SerializedName("email_type") val emailType: String? = null,
    @SerializedName("notice_number") val noticeNumber: String? = null,
    @SerializedName("_source_file") val sourceFile: String? = null,
    @SerializedName("_email_date") val emailDate: String? = null,
    @SerializedName("_subject") val subject: String? = null,
    val roleCategory: String = "General"
)

data class PackageSalary(
    @SerializedName("stipend") val stipend: String? = null,
    @SerializedName("ppo_ctc") val ppoCtc: String? = null,
    @SerializedName("bond") val bond: String? = null
)

data class RoundDetail(
    @SerializedName("round_number") val roundNumber: Int? = null,
    @SerializedName("round_name") val roundName: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("selected_count") val selectedCount: Int? = null
)
