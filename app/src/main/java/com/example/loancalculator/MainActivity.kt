package com.example.loancalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LoanCalculatorScreen() } }
    }
}

private fun num(s: String) = s.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
private fun money(v: Double) = "৳ " + String.format(Locale.US, "%,.2f", v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCalculatorScreen() {
    var category by remember { mutableStateOf("সরকারি চাকরিজীবী") }
    var salary by remember { mutableStateOf("60000") }
    var age by remember { mutableStateOf("30") }
    var maxAge by remember { mutableStateOf("60") }
    var multiplier by remember { mutableStateOf("10") }
    var dbr by remember { mutableStateOf("50") }
    var existingEmi by remember { mutableStateOf("0") }
    var loan by remember { mutableStateOf("500000") }
    var annualRate by remember { mutableStateOf("12") }
    var months by remember { mutableStateOf("60") }
    var flat by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }

    val s = num(salary)
    val a = num(age)
    val retirement = num(maxAge)
    val mult = num(multiplier)
    val dbrPct = num(dbr).coerceIn(0.0, 100.0)
    val old = num(existingEmi)
    val p = num(loan)
    val rate = num(annualRate)
    val n = num(months).toInt().coerceIn(0, 600)
    val maxMonths = ((retirement - a).coerceAtLeast(0.0) * 12).toInt()
    val eligibleMonths = minOf(n, maxMonths)
    val monthlyRate = rate / 1200.0

    fun factor(term: Int): Double {
        if (term <= 0) return 0.0
        if (flat) return (1.0 + monthlyRate * term) / term
        if (monthlyRate == 0.0) return 1.0 / term
        val x = (1.0 + monthlyRate).pow(term)
        return monthlyRate * x / (x - 1.0)
    }

    val emi = p * factor(n)
    val total = emi * n
    val interest = (total - p).coerceAtLeast(0.0)
    val monthlyCapacity = (s * dbrPct / 100.0 - old).coerceAtLeast(0.0)
    val maxByEmi = if (eligibleMonths > 0) monthlyCapacity / factor(eligibleMonths) else 0.0
    val maxBySalary = s * mult
    val eligible = minOf(maxByEmi, maxBySalary).coerceAtLeast(0.0)
    val actualDbr = if (s > 0) (emi + old) / s * 100.0 else 0.0
    val qualified = s > 0 && n > 0 && n <= maxMonths && p <= eligible + 0.01

    val schedule = remember(p, rate, n, flat) {
        val rows = mutableListOf<Triple<Int, Double, Double>>()
        var balance = p
        for (month in 1..n) {
            val interestPart = if (flat) p * monthlyRate else balance * monthlyRate
            val payment = if (flat) p / n + interestPart else p * factor(n)
            val principalPart = if (flat) p / n else (payment - interestPart).coerceIn(0.0, balance)
            balance = (balance - principalPart).coerceAtLeast(0.0)
            rows.add(Triple(month, payment, balance))
        }
        rows
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Personal Loan Calculator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("ঋণের যোগ্যতা, EMI ও কিস্তির হিসাব", color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionTitle("১. গ্রাহকের তথ্য")
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("চাকরির ক্যাটাগরি") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("সরকারি চাকরিজীবী", "ব্যাংকার (নিজ ব্যাংক)", "ব্যাংকার (অন্য ব্যাংক)", "স্কুল শিক্ষক", "বেসরকারি চাকরিজীবী", "অন্যান্য").forEach {
                    DropdownMenuItem(text = { Text(it) }, onClick = { category = it; expanded = false })
                }
            }
        }
        InputField("মাসিক নেট বেতন (৳)", salary) { salary = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { InputField("বর্তমান বয়স", age) { age = it } }
            Box(Modifier.weight(1f)) { InputField("সর্বোচ্চ বয়স", maxAge) { maxAge = it } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { InputField("বেতনের কত গুণ", multiplier) { multiplier = it } }
            Box(Modifier.weight(1f)) { InputField("সর্বোচ্চ DBR (%)", dbr) { dbr = it } }
        }
        InputField("বর্তমান মাসিক কিস্তি (৳)", existingEmi) { existingEmi = it }

        SectionTitle("২. লোনের তথ্য")
        InputField("লোনের পরিমাণ (৳)", loan) { loan = it }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { InputField("বার্ষিক সুদ (%)", annualRate) { annualRate = it } }
            Box(Modifier.weight(1f)) { InputField("মেয়াদ (মাস)", months) { months = it } }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Flat সুদ পদ্ধতি", modifier = Modifier.weight(1f))
            Switch(checked = flat, onCheckedChange = { flat = it })
            Text(if (flat) "Flat" else "Reducing")
        }

        SectionTitle("৩. হিসাবের ফলাফল")
        Card {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("সর্বোচ্চ সম্ভাব্য লোন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(money(eligible), style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                ResultRow("বেতনের ভিত্তিতে সীমা", money(maxBySalary))
                ResultRow("DBR অনুযায়ী কিস্তি সক্ষমতা", money(monthlyCapacity))
                ResultRow("বয়স অনুযায়ী সর্বোচ্চ মেয়াদ", "$maxMonths মাস")
                HorizontalDivider()
                ResultRow("মাসিক EMI", money(emi))
                ResultRow("মোট সুদ", money(interest))
                ResultRow("মোট পরিশোধ", money(total))
                ResultRow("প্রকৃত DBR", String.format(Locale.US, "%.2f%%", actualDbr))
                HorizontalDivider()
                Text(if (qualified) "প্রাথমিকভাবে যোগ্য" else "যোগ্যতার মধ্যে নেই",
                    color = if (qualified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold)
            }
        }

        SectionTitle("৪. কিস্তির Schedule")
        OutlinedButton(onClick = { showSchedule = !showSchedule }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showSchedule) "Schedule বন্ধ করুন" else "Schedule দেখুন")
        }
        if (showSchedule) {
            schedule.take(12).forEach { (month, payment, balance) ->
                ResultRow("মাস $month · কিস্তি ${money(payment)}", "বকেয়া ${money(balance)}")
            }
            if (n > 12) Text("প্রথম ১২ মাস দেখানো হয়েছে। মোট মেয়াদ: $n মাস।",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        OutlinedButton(onClick = {
            salary = "60000"; age = "30"; maxAge = "60"; multiplier = "10"
            dbr = "50"; existingEmi = "0"; loan = "500000"; annualRate = "12"
            months = "60"; flat = false; showSchedule = false
            category = "সরকারি চাকরিজীবী"
        }, modifier = Modifier.fillMaxWidth()) { Text("রিসেট") }

        Text("নোট: এটি প্রাথমিক হিসাব। ব্যাংকের অনুমোদিত নীতিমালা, CIB, আয় ও অন্যান্য শর্ত যাচাই করে চূড়ান্ত সিদ্ধান্ত নিতে হবে। Salary Multiple, DBR ও বয়সসীমা উদাহরণ; ব্যাংকের প্রকৃত নীতিমালা অনুযায়ী পরিবর্তন করুন।",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun InputField(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { onValue(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) }, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
