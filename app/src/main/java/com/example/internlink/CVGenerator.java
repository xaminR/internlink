package com.example.internlink;

import android.content.Context;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class CVGenerator {

    public static String generateCV(Context context, String uid, Map<String, Object> profileData) {
        String fileName = "CV_" + uid + ".pdf";
        File file = new File(context.getFilesDir(), fileName);

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Name
            document.add(new Paragraph(String.valueOf(profileData.getOrDefault("name", "N/A")))
                    .setFontSize(20).setTextAlignment(TextAlignment.CENTER));

            // Contact Info
            document.add(new Paragraph("Phone: " + profileData.getOrDefault("phone", "N/A"))
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Email: " + profileData.getOrDefault("email", "N/A"))
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Address: " + profileData.getOrDefault("address", "N/A"))
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            document.add(new Paragraph("ACADEMIC DETAILS").setFontSize(14));
            document.add(new Paragraph("University: " + profileData.getOrDefault("university", "N/A")));
            document.add(new Paragraph("Semester: " + profileData.getOrDefault("semester", "N/A")));
            document.add(new Paragraph("Courses: " + profileData.getOrDefault("courses", "N/A")));

            document.add(new Paragraph("\n"));

            // Skills
            document.add(new Paragraph("SKILLS").setFontSize(14));
            document.add(new Paragraph(String.valueOf(profileData.getOrDefault("skills", "N/A"))));
            document.add(new Paragraph("Programming Languages: " + profileData.getOrDefault("programmingLanguages", "N/A")));

            document.add(new Paragraph("\n"));

            // Education
            document.add(new Paragraph("EDUCATION").setFontSize(14));
            document.add(new Paragraph(String.valueOf(profileData.getOrDefault("education", "N/A"))));

            document.add(new Paragraph("\n"));

            // Projects
            document.add(new Paragraph("PROJECTS").setFontSize(14));
            document.add(new Paragraph(String.valueOf(profileData.getOrDefault("projects", "N/A"))));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("ADDITIONAL").setFontSize(14));
            document.add(new Paragraph("Hobbies: " + profileData.getOrDefault("hobbies", "N/A")));
            document.add(new Paragraph("LinkedIn: " + profileData.getOrDefault("linkedin", "N/A")));
            document.add(new Paragraph("GitHub: " + profileData.getOrDefault("github", "N/A")));

            document.close();
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
