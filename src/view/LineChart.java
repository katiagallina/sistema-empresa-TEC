package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

public class LineChart extends JPanel {
    private String title;
    private Map<Integer, Double> data;
    private boolean isMonthly; // true for Jan-Dec (1-12), false for days (1-30+)
    private boolean highlightNegativeInRed;
    private Color lineColor = new Color(33, 150, 243); // Material Blue default
    private Color fillColor = new Color(33, 150, 243, 30); // Semi-transparent Blue default
    private Color panelBackgroundColor = null; // if set, uses vibrant style
    private static final String[] MESES = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
    private DecimalFormat dfShort = new DecimalFormat("#,##0");

    public LineChart(String title, Map<Integer, Double> data, boolean isMonthly, boolean highlightNegativeInRed) {
        this.title = title;
        this.data = data != null ? new TreeMap<>(data) : new TreeMap<>();
        this.isMonthly = isMonthly;
        this.highlightNegativeInRed = highlightNegativeInRed;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    public LineChart(String title, Map<Integer, Double> data, boolean isMonthly, boolean highlightNegativeInRed, Color lineColor) {
        this(title, data, isMonthly, highlightNegativeInRed);
        this.lineColor = lineColor;
        this.fillColor = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 30);
    }

    public LineChart(String title, Map<Integer, Double> data, boolean isMonthly, boolean highlightNegativeInRed, Color lineColor, Color panelBackgroundColor) {
        this(title, data, isMonthly, highlightNegativeInRed, lineColor);
        this.panelBackgroundColor = panelBackgroundColor;
    }

    public void setData(Map<Integer, Double> data) {
        this.data = data != null ? new TreeMap<>(data) : new TreeMap<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Determine style colors
        Color titleColor;
        Color gridColor;
        Color labelColor;
        Color drawLineColor;
        Color drawFillColor;
        Color drawFillColorEnd;
        Color borderColor;

        if (panelBackgroundColor != null) {
            titleColor = Color.WHITE;
            gridColor = new Color(255, 255, 255, 45);
            labelColor = new Color(240, 240, 240);
            drawLineColor = Color.WHITE;
            drawFillColor = new Color(255, 255, 255, 45);
            drawFillColorEnd = new Color(255, 255, 255, 0);
            borderColor = new Color(255, 255, 255, 60);
        } else {
            titleColor = new Color(55, 71, 79);
            gridColor = new Color(230, 230, 230);
            labelColor = new Color(120, 120, 120);
            drawLineColor = lineColor;
            drawFillColor = fillColor;
            drawFillColorEnd = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 0);
            borderColor = new Color(225, 230, 235);
        }

        // Draw Background Card
        if (panelBackgroundColor != null) {
            g2.setColor(panelBackgroundColor);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.fillRoundRect(2, 2, width - 4, height - 4, 16, 16);

        // 1. Draw Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(titleColor);
        g2.drawString(title, 15, 25);

        if (data == null || data.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(panelBackgroundColor != null ? new Color(230, 230, 230) : Color.GRAY);
            g2.drawString("Sem dados para exibir", width / 2 - 60, height / 2);
            return;
        }

        // Layout boundaries
        int paddingLeft = 60;
        int paddingRight = 25;
        int paddingTop = 45;
        int paddingBottom = 40;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        if (chartWidth <= 0 || chartHeight <= 0) return;

        // Find min/max values
        double maxValue = 0.0;
        double minValue = 0.0;
        for (double val : data.values()) {
            if (val > maxValue) maxValue = val;
            if (val < minValue) minValue = val;
        }
        if (maxValue == 0 && minValue == 0) {
            maxValue = 100.0; // Avoid divide by zero
        }
        
        // Add 15% margin on top/bottom of y-axis for aesthetic spacing
        double valDiff = maxValue - minValue;
        maxValue += valDiff * 0.15;
        if (minValue < 0) {
            minValue -= valDiff * 0.15;
        } else {
            minValue = 0; // always show 0 baseline if all values positive
        }
        valDiff = maxValue - minValue;
        if (valDiff == 0) valDiff = 1.0;

        // 2. Draw Y Axis Gridlines and Labels
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        int gridCount = 4;
        Stroke originalStroke = g2.getStroke();
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);

        for (int i = 0; i <= gridCount; i++) {
            double gridVal = minValue + (valDiff * i / gridCount);
            int y = paddingTop + chartHeight - (int) ((gridVal - minValue) / valDiff * chartHeight);
            
            // Draw grid line
            g2.setStroke(dashed);
            g2.setColor(gridColor);
            g2.drawLine(paddingLeft, y, paddingLeft + chartWidth, y);
            
            // Draw label
            g2.setColor(labelColor);
            g2.setStroke(originalStroke);
            String label = "R$ " + dfShort.format(gridVal);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, paddingLeft - fm.stringWidth(label) - 8, y + 4);
        }

        // 3. Prepare data points
        int size = data.size();
        Point2D.Double[] points = new Point2D.Double[size];
        int idx = 0;
        
        // Calculate points
        double stepX = (double) chartWidth / (size > 1 ? (size - 1) : 1);
        for (Map.Entry<Integer, Double> entry : data.entrySet()) {
            double x = paddingLeft + (idx * stepX);
            double y = paddingTop + chartHeight - ((entry.getValue() - minValue) / valDiff * chartHeight);
            points[idx] = new Point2D.Double(x, y);
            idx++;
        }

        // 4. Draw fill area under the line
        if (size > 1) {
            Path2D.Double fillPath = new Path2D.Double();
            fillPath.moveTo(points[0].x, paddingTop + chartHeight);
            for (int i = 0; i < size; i++) {
                fillPath.lineTo(points[i].x, points[i].y);
            }
            fillPath.lineTo(points[size - 1].x, paddingTop + chartHeight);
            fillPath.closePath();

            // Create gradient paint for fill
            GradientPaint gp = new GradientPaint(
                (float) paddingLeft, (float) paddingTop, drawFillColor,
                (float) paddingLeft, (float) (paddingTop + chartHeight), drawFillColorEnd
            );
            g2.setPaint(gp);
            g2.fill(fillPath);
        }

        // 5. Draw the chart line
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(drawLineColor);
        if (size > 1) {
            Path2D.Double linePath = new Path2D.Double();
            linePath.moveTo(points[0].x, points[0].y);
            for (int i = 1; i < size; i++) {
                linePath.lineTo(points[i].x, points[i].y);
            }
            g2.draw(linePath);
        } else if (size == 1) {
            g2.fill(new Ellipse2D.Double(points[0].x - 4, points[0].y - 4, 8, 8));
        }

        // 6. Draw dots, labels, and X axis ticks
        g2.setStroke(originalStroke);
        idx = 0;
        for (Map.Entry<Integer, Double> entry : data.entrySet()) {
            Point2D.Double pt = points[idx];
            double val = entry.getValue();

            // Determine colors
            Color ptColor = drawLineColor;
            if (highlightNegativeInRed && val <= 0) {
                ptColor = (panelBackgroundColor != null) ? new Color(255, 110, 110) : new Color(229, 57, 53);
            }

            // Draw dot
            g2.setColor(ptColor);
            g2.fill(new Ellipse2D.Double(pt.x - 4, pt.y - 4, 8, 8));
            g2.setColor(panelBackgroundColor != null ? panelBackgroundColor : Color.WHITE);
            g2.fill(new Ellipse2D.Double(pt.x - 2, pt.y - 2, 4, 4));

            // Draw value text on top of dot (for key months, or limit for days)
            boolean showValLabel = true;
            if (!isMonthly) {
                showValLabel = (entry.getKey() == 1 || entry.getKey() % 5 == 0 || entry.getKey() == size);
            }

            if (showValLabel) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g2.setColor(ptColor);
                String valStr = dfShort.format(val);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(valStr);
                g2.drawString(valStr, (float) (pt.x - textW / 2), (float) (pt.y - 8));
            }

            // Draw X label
            boolean showXLabel = true;
            String xLabel = "";
            if (isMonthly) {
                int mesIdx = entry.getKey() - 1;
                if (mesIdx >= 0 && mesIdx < MESES.length) {
                    xLabel = MESES[mesIdx];
                } else {
                    xLabel = String.valueOf(entry.getKey());
                }
            } else {
                xLabel = String.valueOf(entry.getKey());
                showXLabel = (entry.getKey() == 1 || entry.getKey() % 5 == 0 || entry.getKey() == size);
            }

            if (showXLabel) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(labelColor);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(xLabel);
                g2.drawString(xLabel, (float) (pt.x - textW / 2), (float) (paddingTop + chartHeight + 18));
            }

            idx++;
        }

        // Draw a border around the chart card
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(2, 2, width - 4, height - 4, 16, 16);
    }
}
