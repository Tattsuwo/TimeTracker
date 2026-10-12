package com.example.timetracker.backup

import com.example.timetracker.data.SessionWithCategory
import com.example.timetracker.util.durationBetween
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Génère un classeur Excel (.xlsx) listant toutes les sessions, de la plus
 * ancienne à la plus récente, avec les colonnes :
 * Date | Heure début | Heure fin | Durée | Projet | Description | Catégorie
 *
 * Aucune bibliothèque externe : un fichier .xlsx n'est qu'une archive ZIP de
 * quelques fichiers XML (format "Office Open XML"), que java.util.zip (déjà
 * fourni par Android) suffit à produire. On évite ainsi une dépendance lourde
 * comme Apache POI, mal adaptée à Android.
 *
 * La date, les heures et la durée sont écrites comme de VRAIES valeurs Excel
 * (nombres) affichées via un format de cellule, et non comme du texte : on peut
 * donc trier, filtrer ou additionner les durées directement dans Excel.
 * Formats d'affichage : jj/mm/aaaa pour la date, hh:mm pour les heures et la
 * durée.
 */
class ExcelExporter {

    /** Construit le contenu complet du fichier .xlsx (octets de l'archive ZIP). */
    fun build(sessions: List<SessionWithCategory>): ByteArray {
        val ordered = sessions.sortedWith(compareBy<SessionWithCategory>({ it.session.startTime }, { it.session.id }))
        val lastRow = ordered.size + 1 // +1 : la ligne 1 contient les en-têtes

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.addEntry("[Content_Types].xml", CONTENT_TYPES_XML)
            zip.addEntry("_rels/.rels", ROOT_RELS_XML)
            zip.addEntry("xl/workbook.xml", buildWorkbookXml(lastRow))
            zip.addEntry("xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
            zip.addEntry("xl/styles.xml", STYLES_XML)
            zip.addEntry("xl/worksheets/sheet1.xml", buildSheetXml(ordered, lastRow))
        }
        return output.toByteArray()
    }

    private fun ZipOutputStream.addEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }
    // Ajoute un fichier texte (UTF-8) à l'archive ZIP.

    private fun buildWorkbookXml(lastRow: Int): String {
        // Nom défini "_FilterDatabase" : c'est ce qu'Excel écrit lui-même pour
        // accompagner un filtre automatique ; "\$" est un "$" littéral dans une
        // chaîne Kotlin (les "$" des références de cellules Excel, ex. $A$1).
        val filterRange = "Sessions!\$A\$1:\$G\$$lastRow"
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
            """<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" """ +
            """xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">""" +
            """<sheets><sheet name="Sessions" sheetId="1" r:id="rId1"/></sheets>""" +
            """<definedNames><definedName name="_xlnm._FilterDatabase" localSheetId="0" hidden="1">""" +
            """$filterRange</definedName></definedNames>""" +
            """</workbook>"""
    }

    private fun buildSheetXml(sessions: List<SessionWithCategory>, lastRow: Int): String {
        // Date et heures affichées dans le fuseau horaire de l'appareil, comme
        // partout ailleurs dans l'appli.
        val zone = ZoneId.systemDefault()
        val sb = StringBuilder()

        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("""<dimension ref="A1:G$lastRow"/>""")
        // Ligne d'en-têtes figée : elle reste visible quand on fait défiler la liste.
        sb.append("""<sheetViews><sheetView workbookViewId="0">""")
        sb.append("""<pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/>""")
        sb.append("""<selection pane="bottomLeft" activeCell="A2" sqref="A2"/>""")
        sb.append("""</sheetView></sheetViews>""")
        sb.append("""<sheetFormatPr defaultRowHeight="15"/>""")
        sb.append("""<cols>""")
        COLUMN_WIDTHS.forEachIndexed { index, width ->
            val column = index + 1
            sb.append("""<col min="$column" max="$column" width="$width" customWidth="1"/>""")
        }
        sb.append("""</cols>""")

        sb.append("""<sheetData>""")
        sb.append("""<row r="1">""")
        HEADERS.forEachIndexed { index, header ->
            sb.append(textCell(COLUMN_LETTERS[index], 1, header, STYLE_HEADER))
        }
        sb.append("""</row>""")

        sessions.forEachIndexed { index, item ->
            val row = index + 2
            val session = item.session
            val start = session.startTime.atZone(zone)
            val end = session.endTime.atZone(zone)

            sb.append("""<row r="$row">""")
            // La date est celle du DÉBUT de la session, comme dans l'appli :
            // une session à cheval sur minuit reste comptée sur son jour de départ.
            sb.append(numberCell("A", row, STYLE_DATE, ChronoUnit.DAYS.between(EXCEL_EPOCH, start.toLocalDate()).toString()))
            sb.append(numberCell("B", row, STYLE_TIME, dayFraction(start.hour * 60L + start.minute)))
            sb.append(numberCell("C", row, STYLE_TIME, dayFraction(end.hour * 60L + end.minute)))
            sb.append(numberCell("D", row, STYLE_DURATION, dayFraction(durationBetween(session.startTime, session.endTime).toMinutes())))
            sb.append(textCell("E", row, session.name, STYLE_TEXT))
            sb.append(textCell("F", row, session.description, STYLE_TEXT))
            sb.append(textCell("G", row, item.categoryName, STYLE_TEXT))
            sb.append("""</row>""")
        }
        sb.append("""</sheetData>""")
        sb.append("""<autoFilter ref="A1:G$lastRow"/>""")
        sb.append("""</worksheet>""")
        return sb.toString()
    }

    /** Cellule numérique (date, heure ou durée) : <c r="A2" s="2"><v>45930</v></c>. */
    private fun numberCell(column: String, row: Int, style: Int, value: String): String =
        """<c r="$column$row" s="$style"><v>$value</v></c>"""

    /**
     * Cellule de texte "en ligne" (inlineStr), qui évite d'avoir à gérer un
     * fichier de chaînes partagées. Une cellule vide n'est simplement pas écrite.
     */
    private fun textCell(column: String, row: Int, text: String, style: Int): String {
        if (text.isEmpty()) return ""
        return """<c r="$column$row" s="$style" t="inlineStr"><is><t xml:space="preserve">${escapeXml(text)}</t></is></c>"""
    }

    /**
     * Excel stocke une heure ou une durée comme une fraction de jour
     * (12:00 = 0,5 ; 1 h = 1/24) : on convertit donc des minutes en fraction de
     * jour (1 jour = 1440 minutes).
     */
    private fun dayFraction(minutes: Long): String = (minutes / 1440.0).toString()

    /**
     * Échappe les caractères réservés du XML et supprime ceux qui y sont
     * interdits (certains caractères de contrôle) : sans ça, un seul caractère
     * invalide dans une description suffirait à rendre le fichier illisible pour
     * Excel. Les caractères hors du plan de base (ex. emojis) sont conservés.
     */
    private fun escapeXml(raw: String): String {
        val sb = StringBuilder(raw.length + 16)
        var i = 0
        while (i < raw.length) {
            val cp = raw.codePointAt(i)
            i += Character.charCount(cp)
            when {
                cp == '&'.code -> sb.append("&amp;")
                cp == '<'.code -> sb.append("&lt;")
                cp == '>'.code -> sb.append("&gt;")
                cp == 0x9 || cp == 0xA || cp == 0xD ||
                    cp in 0x20..0xD7FF || cp in 0xE000..0xFFFD || cp in 0x10000..0x10FFFF ->
                    sb.appendCodePoint(cp)
                // Sinon : caractère interdit en XML 1.0, ignoré.
            }
        }
        return sb.toString()
    }

    private companion object {
        // Excel compte les dates en jours depuis le 30/12/1899 (et non le 1/1/1900,
        // à cause d'un vieux bug de compatibilité avec Lotus 1-2-3 conservé
        // volontairement par Excel : pour toute date après février 1900, le
        // décalage de 30/12/1899 est le bon).
        val EXCEL_EPOCH: LocalDate = LocalDate.of(1899, 12, 30)

        val HEADERS = listOf("Date", "Heure début", "Heure fin", "Durée", "Projet", "Description", "Catégorie")
        val COLUMN_LETTERS = listOf("A", "B", "C", "D", "E", "F", "G")
        val COLUMN_WIDTHS = listOf(12, 12, 10, 10, 30, 50, 20)

        // Indices des styles définis dans STYLES_XML (ordre de <cellXfs>).
        const val STYLE_HEADER = 1
        const val STYLE_DATE = 2
        const val STYLE_TIME = 3
        const val STYLE_DURATION = 4
        const val STYLE_TEXT = 5

        val CONTENT_TYPES_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
            <Default Extension="xml" ContentType="application/xml"/>
            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
            <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
            </Types>
        """.trimIndent()

        val ROOT_RELS_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
            </Relationships>
        """.trimIndent()

        val WORKBOOK_RELS_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
            <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
            </Relationships>
        """.trimIndent()

        // Formats d'affichage personnalisés (identifiants 164 et suivants) :
        //   164 = jj/mm/aaaa      165 = hh:mm      166 = [hh]:mm
        // [hh]:mm (hh entre crochets) = durée écoulée : les heures ne repartent
        // pas à zéro après 24 h, et sont affichées sur 2 chiffres (01:32).
        val STYLES_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
            <numFmts count="3">
            <numFmt numFmtId="164" formatCode="dd/mm/yyyy"/>
            <numFmt numFmtId="165" formatCode="hh:mm"/>
            <numFmt numFmtId="166" formatCode="[hh]:mm"/>
            </numFmts>
            <fonts count="2">
            <font><sz val="11"/><name val="Calibri"/></font>
            <font><b/><sz val="11"/><name val="Calibri"/></font>
            </fonts>
            <fills count="2">
            <fill><patternFill patternType="none"/></fill>
            <fill><patternFill patternType="gray125"/></fill>
            </fills>
            <borders count="1">
            <border><left/><right/><top/><bottom/><diagonal/></border>
            </borders>
            <cellStyleXfs count="1">
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
            </cellStyleXfs>
            <cellXfs count="6">
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
            <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
            <xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyAlignment="1"><alignment vertical="top"/></xf>
            <xf numFmtId="165" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyAlignment="1"><alignment vertical="top"/></xf>
            <xf numFmtId="166" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyAlignment="1"><alignment vertical="top"/></xf>
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0" applyAlignment="1"><alignment vertical="top" wrapText="1"/></xf>
            </cellXfs>
            <cellStyles count="1">
            <cellStyle name="Normal" xfId="0" builtinId="0"/>
            </cellStyles>
            </styleSheet>
        """.trimIndent()
    }
}
