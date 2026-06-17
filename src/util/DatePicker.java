package util;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePicker extends JPanel {

    private JTextField txtDate;
    private JButton btnCalendar;
    private Date selectedDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public DatePicker() {
        this(new Date());
    }

    public DatePicker(Date defaultDate) {
        setLayout(new BorderLayout(5, 0));
        selectedDate = defaultDate;

        txtDate = new JTextField(10);
        txtDate.setEditable(false);
        txtDate.setText(sdf.format(selectedDate));
        
        btnCalendar = new JButton("📅");
        btnCalendar.setFocusPainted(false);
        btnCalendar.setMargin(new Insets(2, 5, 2, 5));

        add(txtDate, BorderLayout.CENTER);
        add(btnCalendar, BorderLayout.EAST);

        btnCalendar.addActionListener(e -> showCalendarDialog());
    }

    public Date getValue() {
        return selectedDate;
    }

    public void setValue(Date date) {
        if (date != null) {
            selectedDate = date;
            txtDate.setText(sdf.format(selectedDate));
        }
    }

    private void showCalendarDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        CalendarDialog dialog = new CalendarDialog(parentWindow, selectedDate);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            setValue(dialog.getSelectedDate());
        }
    }

    private static class CalendarDialog extends JDialog {
        private Calendar calendar;
        private Date selectedDate;
        private boolean confirmed = false;
        
        private JLabel lblMonthYear;
        private JPanel daysPanel;
        private JButton[] dayButtons = new JButton[42];
        private String[] months = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", 
                                   "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        public CalendarDialog(Window owner, Date initialDate) {
            super(owner, "Selecionar Data", ModalityType.APPLICATION_MODAL);
            setSize(320, 280);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(5, 5));

            calendar = Calendar.getInstance();
            calendar.setTime(initialDate);
            selectedDate = initialDate;

            // Painel Superior: Mês/Ano e Navegação
            JPanel topPanel = new JPanel(new BorderLayout());
            JButton btnPrev = new JButton("<");
            JButton btnNext = new JButton(">");
            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 14));

            topPanel.add(btnPrev, BorderLayout.WEST);
            topPanel.add(lblMonthYear, BorderLayout.CENTER);
            topPanel.add(btnNext, BorderLayout.EAST);
            add(topPanel, BorderLayout.NORTH);

            // Painel de Dias
            JPanel gridContainer = new JPanel(new BorderLayout(2, 2));
            
            // Cabeçalho dos dias da semana
            JPanel headerPanel = new JPanel(new GridLayout(1, 7));
            String[] weekDays = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
            for (String day : weekDays) {
                JLabel lbl = new JLabel(day, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setForeground(Color.GRAY);
                headerPanel.add(lbl);
            }
            gridContainer.add(headerPanel, BorderLayout.NORTH);

            // Grid dos dias do mês
            daysPanel = new JPanel(new GridLayout(6, 7, 2, 2));
            for (int i = 0; i < 42; i++) {
                dayButtons[i] = new JButton();
                dayButtons[i].setFocusPainted(false);
                dayButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 11));
                int index = i;
                dayButtons[i].addActionListener(e -> {
                    String text = dayButtons[index].getText();
                    if (!text.isEmpty()) {
                        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(text));
                        selectedDate = calendar.getTime();
                        confirmed = true;
                        dispose();
                    }
                });
                daysPanel.add(dayButtons[i]);
            }
            gridContainer.add(daysPanel, BorderLayout.CENTER);
            add(gridContainer, BorderLayout.CENTER);

            // Ações de navegação
            btnPrev.addActionListener(e -> {
                calendar.add(Calendar.MONTH, -1);
                updateCalendar();
            });
            btnNext.addActionListener(e -> {
                calendar.add(Calendar.MONTH, 1);
                updateCalendar();
            });

            updateCalendar();
        }

        private void updateCalendar() {
            lblMonthYear.setText(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
            
            Calendar tempCal = (Calendar) calendar.clone();
            tempCal.set(Calendar.DAY_OF_MONTH, 1);
            int startDay = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
            int maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Limpar todos os botões
            for (int i = 0; i < 42; i++) {
                dayButtons[i].setText("");
                dayButtons[i].setEnabled(false);
                dayButtons[i].setBackground(null);
            }

            // Preencher os dias do mês
            for (int day = 1; day <= maxDays; day++) {
                int buttonIndex = startDay + day - 1;
                dayButtons[buttonIndex].setText(String.valueOf(day));
                dayButtons[buttonIndex].setEnabled(true);

                // Destacar o dia selecionado
                Calendar compareCal = Calendar.getInstance();
                compareCal.setTime(selectedDate);
                if (tempCal.get(Calendar.YEAR) == compareCal.get(Calendar.YEAR) &&
                    tempCal.get(Calendar.MONTH) == compareCal.get(Calendar.MONTH) &&
                    day == compareCal.get(Calendar.DAY_OF_MONTH)) {
                    dayButtons[buttonIndex].setBackground(new Color(0, 120, 215));
                    dayButtons[buttonIndex].setForeground(Color.WHITE);
                } else {
                    dayButtons[buttonIndex].setForeground(Color.BLACK);
                }
            }
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public Date getSelectedDate() {
            return selectedDate;
        }
    }
}
