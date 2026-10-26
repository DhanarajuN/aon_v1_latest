package com.ongo.aonsecure.pdf_gen;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TemplatePDF {

    public static final BaseColor COLOR_ACCENT = new BaseColor(104, 2, 5);
    public static final BaseColor COLOR_GREY = new BaseColor(220, 220, 220);

    private String tagLogErr = "openDocument";
    private Context context;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private Paragraph paragraph;
    private Font fTitle = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
    private Font fSubTitle = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.BOLD);
    private Font fText = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private Font fHighText = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.BOLD, COLOR_ACCENT);
    private Font fTotalValue = new Font(Font.FontFamily.TIMES_ROMAN, 22, Font.BOLD, BaseColor.BLACK);


    private Font fSubTitle_WHITE = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.WHITE);
    private Font fHighText_WHITE = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.WHITE);

    private String quoteNumberForPDF = "";

    public TemplatePDF(Context context) {
        this.context = context;
    }

    public void openDocument(String quoteNo) {

        try {
            createFile(context,quoteNo);
            document = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
        } catch (Exception e) {
            Log.e(tagLogErr, e.toString());
        }

    }

    public void addBorder() throws DocumentException {
/*        PdfContentByte canvas = pdfWriter.getDirectContent();
        Rectangle rect = document.getPageSize();
        rect = PageSize.getRectangle("LETTER");
        rect.setBorder(Rectangle.BOX); // left, right, top, bottom border
        rect.setBorderWidth(5); // a width of 5 user units
        rect.setBorderColor(BaseColor.RED); // a red border
        rect.setUseVariableBorders(true); // the full width will be visible
        canvas.rectangle(rect);
        document.add(rect);*/

        Rectangle rect = new Rectangle(577, 825, 18, 15); // you can resize rectangle
        rect.enableBorderSide(1);
        rect.enableBorderSide(2);
        rect.enableBorderSide(4);
        rect.enableBorderSide(8);
        rect.setBorderColor(COLOR_ACCENT);
        rect.setBorderWidth(1);
        document.add(rect);
    }


    private void createFile(Context context, String quoteNo) {
        File folder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        pdfFile = new File(folder, quoteNo + ".pdf");
    }



    public void closeDocument() {
        document.close();
    }

    public void addMetaData(String title, String subject, String author) {
        document.addTitle(title);
        document.addSubject(subject);
        document.addAuthor(author);
    }

    public void addTitles(String title, String subTitle, String client, String value) {

        try {

            paragraph = new Paragraph();
            addChield(new Paragraph(title, fTitle));
            addChield(new Paragraph(subTitle, fSubTitle));
            addChield(new Paragraph(client, fHighText));
            addChield(new Paragraph(value, fTotalValue));

            paragraph.setSpacingAfter(30);
            document.add(paragraph);

        } catch (Exception e) {
            Log.e(tagLogErr, e.toString());
        }

    }

    public void lineSeperator() throws DocumentException {
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(COLOR_ACCENT);
        document.add(new Paragraph(""));
        document.add(new Chunk(lineSeparator));
        document.add(new Paragraph(""));
    }

    public void createHeaderTable(String[] value, String textType, byte[] byteArry) throws IOException, DocumentException {
        PdfPTable mainTable = new PdfPTable(2);
        mainTable.getDefaultCell().setBorder(0);
        mainTable.setWidthPercentage(100);
        mainTable.setPaddingTop(18);

        PdfPCell imageCell = new PdfPCell();
        Image i = Image.getInstance(byteArry);
        i.scaleAbsolute(200f, 200f);
        imageCell.addElement(new Chunk(i, 5, -5));
        mainTable.addCell(imageCell);
        paragraph.add(mainTable);

        document.add(new Paragraph(""));
        document.add(new Paragraph(""));
        document.add(paragraph);
    }

    public void addSubTitleParagraph(String subTitle, String alignment) {

        try {

            paragraph = new Paragraph();
            if (alignment.equalsIgnoreCase("center"))
                addChildToCenter(new Paragraph(subTitle, fSubTitle));
            else
                addChildToLeft(new Paragraph(subTitle, fSubTitle));
            paragraph.setSpacingAfter(5);
            paragraph.setPaddingTop(15);
            document.add(paragraph);

        } catch (Exception e) {
            Log.e(tagLogErr, e.toString());
        }

    }

    public void addImage(byte[] byteArry) {
        try {
            //if you would have a chapter indentation
            paragraph = new Paragraph();
            Image i = Image.getInstance(byteArry);
            i.scaleToFit(80f, 80f);
            Chunk c = new Chunk(i, 0, -54);
            paragraph.add(c);
            document.add(paragraph);
        } catch (Exception e) {
            Log.e("image", e.toString());
        }
    }

    public void addvalueParagraph(String title) {

        try {

            paragraph = new Paragraph();
            addChildToLeft(new Paragraph(title, fHighText));
            paragraph.setSpacingAfter(3);
            document.add(paragraph);

        } catch (Exception e) {
            Log.e(tagLogErr, e.toString());
        }

    }

    private void addChildToCenter(Paragraph childParagraph) {
        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }


    private void addChildToLeft(Paragraph childParagraph) {
        childParagraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.add(childParagraph);
    }

    public void tableWithBackgroundColor(String[] value, String textType, ArrayList<String[]> clientsT) {
        try {
            ArrayList<String[]> clients = null;

            if (clients == null) {
                clients = new ArrayList<>();
                clients.add(value);
            }

            paragraph = new Paragraph();
            if (textType.equalsIgnoreCase("fSubTitle"))
                paragraph.setFont(fSubTitle);
            else
                paragraph.setFont(fHighText);

            PdfPTable pdfPTable = new PdfPTable(value.length);
            pdfPTable.getDefaultCell().setBorder(0);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.setPaddingTop(8);
            PdfPCell pdfPCell;

            int indexC = 0;


            for (int indexR = 0; indexR < clients.size(); indexR++) {
                String[] row = clients.get(indexR);
                for (indexC = 0; indexC < value.length; indexC++) {
                    if (indexC == 0 || indexC == 1) {
                        if (textType.equalsIgnoreCase("fSubTitle"))
                            pdfPCell = new PdfPCell(new Paragraph(new Phrase(row[indexC]).get(0).toString(), fSubTitle));
                        else
                            pdfPCell = new PdfPCell(new Paragraph(new Phrase(row[indexC]).get(0).toString(), fHighText));
                    } else {
                        if (textType.equalsIgnoreCase("fSubTitle"))
                            pdfPCell = new PdfPCell(new Paragraph(new Phrase(row[indexC]).get(0).toString(), fSubTitle_WHITE));
                        else
                            pdfPCell = new PdfPCell(new Paragraph(new Phrase(row[indexC]).get(0).toString(), fHighText_WHITE));
                    }
                    pdfPCell.setBorder(Rectangle.NO_BORDER);
                    pdfPCell.setPaddingBottom(5);
                    if (!textType.equalsIgnoreCase("fSubTitle"))
                        pdfPCell.setPaddingBottom(5);
                    if (indexC == 0 || indexC == 1) {
                        pdfPCell.setBackgroundColor(COLOR_GREY);
                    } else {
                        pdfPCell.setBackgroundColor(COLOR_ACCENT);
                    }

                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);


                    pdfPTable.addCell(pdfPCell);
                }
            }

            paragraph.add(pdfPTable);

            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(paragraph);

        } catch (Exception e) {
            Log.e(tagLogErr, e.toString());
        }


    }


    public void createTable(String[] value, String textType, ArrayList<String[]> clientsT) {
        try {
            ArrayList<String[]> clients = null;

            if (clients == null) {
                clients = new ArrayList<>();
                clients.add(value);
            }

            paragraph = new Paragraph();
            if (textType.equalsIgnoreCase("fSubTitle"))
                paragraph.setFont(fSubTitle);
            else
                paragraph.setFont(fHighText);
            PdfPTable pdfPTable = new PdfPTable(value.length);
            pdfPTable.getDefaultCell().setBorder(0);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.setPaddingTop(8);
            PdfPCell pdfPCell;

            int indexC = 0;
            for (int indexR = 0; indexR < clients.size(); indexR++) {
                String[] row = clients.get(indexR);
                for (indexC = 0; indexC < value.length; indexC++) {
                    if (textType.equalsIgnoreCase("fSubTitle"))
                        pdfPCell = new PdfPCell(new Paragraph(new Phrase(row[indexC]).get(0).toString(), fSubTitle));
                    else
                        pdfPCell = new PdfPCell(new Paragraph(new Phrase(row[indexC]).get(0).toString(), fHighText));
                    pdfPCell.setBorder(Rectangle.NO_BORDER);
                    pdfPCell.setPaddingBottom(5);
                    if (!textType.equalsIgnoreCase("fSubTitle"))
                        pdfPCell.setPaddingBottom(10);
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);


                    pdfPTable.addCell(pdfPCell);
                }
            }

            paragraph.add(pdfPTable);

            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(paragraph);
        } catch (Exception e) {
            Log.e(tagLogErr, e.toString());
        }
    }

    public void addParagraph(String text) {

        try {
            paragraph = new Paragraph(text, fText);
            paragraph.setSpacingAfter(5);
            paragraph.setSpacingBefore(5);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);

        } catch (Exception e) {
            Log.e(tagLogErr, e.toString());
        }

    }

    private void addChield(Paragraph childParagraph) {
        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }

    public void viewPDF(Context context) {

        if (pdfFile != null && pdfFile.exists()) {

            try {
                // Correct file (actual saved location)
                File file = pdfFile;

                // Correct FileProvider URI
                Uri uri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        file
                );

                // ====== SHARE PDF =======
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // ====== VIEW PDF ========
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(uri, "application/pdf");
                viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // ====== CHOOSE ACTION (User picks Viewer or Share App) =======
                Intent chooser = Intent.createChooser(shareIntent, "Open or Share PDF");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{viewIntent});

                context.startActivity(chooser);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PDF", "Error opening PDF: " + e.getMessage());
            }

        } else {
            Log.e("PDF", "PDF File is null or does not exist");
        }
    }

}

class BorderEvent implements PdfPTableEvent {
    public void tableLayout(PdfPTable table, float[][] widths,
                            float[] heights, int headerRows, int rowStart,
                            PdfContentByte[] canvases) {
        float width[] = widths[0];
        float x1 = width[0];
        float x2 = width[width.length - 1];
        float y1 = heights[0];
        float y2 = heights[heights.length - 1];
        PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
        cb.rectangle(x1, y1, x2 - x1, y2 - y1);
        cb.stroke();
        cb.resetRGBColorStroke();
    }
}
