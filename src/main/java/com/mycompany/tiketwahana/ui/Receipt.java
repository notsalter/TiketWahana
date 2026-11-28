package com.mycompany.tiketwahana.ui;

import com.mycompany.tiketwahana.core.Money;
import com.mycompany.tiketwahana.domain.Sale;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;
import javax.swing.JComponent;

/** Hand-painted receipt preview and java.awt.print output. */
public final class Receipt extends JComponent implements Printable {
    private final Sale.Result sale;
    public Receipt(Sale.Result sale) {
        this.sale=sale; setOpaque(true); setBackground(Color.WHITE); setPreferredSize(new Dimension(340,460));
    }
    @Override protected void paintComponent(Graphics g){Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);x.setColor(Color.WHITE);x.fillRect(0,0,getWidth(),getHeight());x.setColor(Color.BLACK);x.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));x.drawString("TIKETWAHANA",24,30);x.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));x.drawString("Taman bermain · tiket masuk",24,48);x.drawLine(24,62,getWidth()-24,62);int y=84;x.setFont(new Font(Font.MONOSPACED,Font.BOLD,11));x.drawString(sale.transactionCode(),24,y);y+=22;x.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));x.drawString("Jumlah tiket  "+sale.ticketCount(),24,y);y+=18;x.drawString("Subtotal      "+Money.plain(sale.subtotal()),24,y);y+=18;x.drawString("Diskon        "+Money.plain(sale.discount()),24,y);y+=18;x.drawString("Total         "+Money.plain(sale.total()),24,y);y+=18;x.drawString("Bayar         "+Money.plain(sale.paid()),24,y);y+=18;x.drawString("Kembalian     "+Money.plain(sale.change()),24,y);y+=20;x.drawLine(24,y,getWidth()-24,y);y+=20;for(String code:sale.ticketCodes()){x.drawString(code,24,y);y+=16;}x.setFont(new Font(Font.MONOSPACED,Font.PLAIN,9));x.drawString("Simpan kode ini untuk validasi masuk.",24,getHeight()-22);x.dispose();}
    @Override public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)throws PrinterException{if(pageIndex>0)return NO_SUCH_PAGE;Graphics2D g=(Graphics2D)graphics;double sx=pageFormat.getImageableWidth()/getPreferredSize().width;double sy=pageFormat.getImageableHeight()/getPreferredSize().height;double scale=Math.min(1,Math.min(sx,sy));g.translate(pageFormat.getImageableX(),pageFormat.getImageableY());g.scale(scale,scale);setSize(getPreferredSize());paint(g);return PAGE_EXISTS;}
    public static void print(Sale.Result sale)throws PrinterException{PrinterJob job=PrinterJob.getPrinterJob();job.setPrintable(new Receipt(sale));if(job.printDialog())job.print();}
}
