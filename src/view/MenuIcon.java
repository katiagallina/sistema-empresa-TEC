package view;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.Icon;

public class MenuIcon implements Icon {
    public enum IconType {
        DASHBOARD, CLIENTS, PRODUCTS, SERVICES, SERVICES_REALIZED, ORCAMENTOS, FINALIZAR_OS, VENTA_RAPIDA, RELATORIOS, CONFIGURACOES
    }

    private final IconType type;
    private final int size;

    public MenuIcon(IconType type) {
        this.type = type;
        this.size = 18;
    }

    public MenuIcon(IconType type, int size) {
        this.type = type;
        this.size = size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        Color iconColor = c.getForeground();
        g2.setColor(iconColor);
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int w = size;
        int h = size;

        switch (type) {
            case DASHBOARD:
                // Wave chart (grafico de ondinhas)
                Path2D.Float wavePath = new Path2D.Float();
                wavePath.moveTo(x + 2, y + 11);
                wavePath.curveTo(x + 6, y + 3, x + 10, y + 19, x + 14, y + 11);
                wavePath.curveTo(x + 16, y + 7, x + 17, y + 13, x + 18, y + 11);
                g2.draw(wavePath);
                
                // Draw dynamic dots on the wave peaks/valleys
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(wavePath);
                break;

            case CLIENTS:
                // User avatars (line art)
                // Back client:
                g2.drawOval(x + 2, y + 6, 5, 5);
                g2.drawArc(x, y + 12, 9, 8, 0, 180);

                // Masking background for front client (to prevent line overlapping)
                g2.setColor(c.getBackground());
                g2.fillOval(x + 8, y + 3, 6, 6);
                g2.fillArc(x + 5, y + 10, 12, 10, 0, 180);

                // Front client:
                g2.setColor(iconColor);
                g2.drawOval(x + 8, y + 3, 6, 6);
                g2.drawArc(x + 5, y + 10, 12, 10, 0, 180);
                break;

            case PRODUCTS:
                // Isometric cardboard box (cube outline)
                int cx = x + 9;
                int cy = y + 8;
                
                // Top face rhombus:
                Path2D.Float topRhombus = new Path2D.Float();
                topRhombus.moveTo(cx, y + 2);
                topRhombus.lineTo(cx + 7, y + 6);
                topRhombus.lineTo(cx, cy);
                topRhombus.lineTo(cx - 7, y + 6);
                topRhombus.closePath();
                g2.draw(topRhombus);
                
                // Vertical pillars:
                g2.drawLine(cx, cy, cx, y + 15);
                g2.drawLine(cx - 7, y + 6, cx - 7, y + 13);
                g2.drawLine(cx + 7, y + 6, cx + 7, y + 13);
                
                // Bottom edges:
                g2.drawLine(cx - 7, y + 13, cx, y + 17);
                g2.drawLine(cx + 7, y + 13, cx, y + 17);
                break;

            case SERVICES:
                // Wrench
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 4, y + 14, x + 11, y + 7);
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Wrench loop at bottom-left:
                g2.drawOval(x + 2, y + 13, 3, 3);
                
                // Wrench head cutout at top-right (center is 13, 5):
                g2.drawArc(x + 9, y + 2, 7, 7, 135, 270);
                break;

            case SERVICES_REALIZED:
                // Clipboard with a tiny green checkmark
                g2.drawRoundRect(x + 3, y + 2, 11, 14, 2, 2);
                g2.drawLine(x + 6, y + 6, x + 11, y + 6);
                g2.drawLine(x + 6, y + 10, x + 11, y + 10);
                
                // Mask background for the checkmark
                g2.setColor(c.getBackground());
                g2.fillOval(x + 10, y + 9, 8, 8);
                
                // Draw checkmark in green:
                g2.setColor(new Color(40, 167, 69)); // Green
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 11, y + 13, x + 13, y + 15);
                g2.drawLine(x + 13, y + 15, x + 17, y + 10);
                break;

            case ORCAMENTOS:
                // Document / spreadsheet page with a dollar sign
                g2.drawRoundRect(x + 3, y + 2, 12, 14, 2, 2);
                g2.drawLine(x + 6, y + 6, x + 12, y + 6);
                g2.drawLine(x + 6, y + 10, x + 9, y + 10);
                
                // Dollar sign text:
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("$", x + 11, y + 13);
                break;

            case FINALIZAR_OS:
                // A badge containing a checkmark
                g2.setColor(new Color(40, 167, 69)); // Green badge
                g2.drawOval(x + 1, y + 1, 16, 16);
                
                // Checkmark:
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 5, y + 9, x + 8, y + 12);
                g2.drawLine(x + 8, y + 12, x + 13, y + 6);
                break;

            case VENTA_RAPIDA:
                // Bolt (lightning) filled with orange/yellow
                Path2D.Float bolt = new Path2D.Float();
                bolt.moveTo(x + 11, y + 1);
                bolt.lineTo(x + 4, y + 10);
                bolt.lineTo(x + 9, y + 10);
                bolt.lineTo(x + 7, y + 17);
                bolt.lineTo(x + 14, y + 8);
                bolt.lineTo(x + 9, y + 8);
                bolt.closePath();
                
                g2.setColor(new Color(255, 193, 7)); // Yellow
                g2.fill(bolt);
                g2.setColor(new Color(230, 126, 34)); // Darker yellow/orange border
                g2.draw(bolt);
                break;

            case RELATORIOS:
                // Bar graph
                // Axes:
                g2.drawLine(x + 2, y + 15, x + 16, y + 15);
                g2.drawLine(x + 2, y + 2, x + 2, y + 15);
                
                // Bars:
                g2.drawRect(x + 4, y + 9, 3, 6);
                g2.drawRect(x + 8, y + 5, 3, 10);
                g2.drawRect(x + 12, y + 11, 3, 4);
                break;

            case CONFIGURACOES:
                // Gear (engrenagem)
                int gcx = x + 9;
                int gcy = y + 9;
                int rIn = 3;
                int rOut = 5;
                
                g2.drawOval(gcx - rIn, gcy - rIn, rIn * 2, rIn * 2);
                g2.drawOval(gcx - rOut, gcy - rOut, rOut * 2, rOut * 2);
                
                // 8 gear teeth:
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4.0;
                    int x1 = (int) (gcx + rOut * Math.cos(angle));
                    int y1 = (int) (gcy + rOut * Math.sin(angle));
                    int x2 = (int) (gcx + (rOut + 2) * Math.cos(angle));
                    int y2 = (int) (gcy + (rOut + 2) * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }
                break;
        }

        g2.dispose();
    }
}
