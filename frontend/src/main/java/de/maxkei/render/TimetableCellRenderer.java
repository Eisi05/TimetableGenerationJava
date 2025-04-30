package de.maxkei.render;

import de.maxkei.objects.TimetableCell;
import de.maxkei.utils.Var;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Renderer for rendering timetable cells in a table.
 */
public class TimetableCellRenderer extends DefaultTableCellRenderer
{
    /**
     * Renders the timetable cell as a component.
     *
     * @param table      the JTable.
     * @param value      the value of the cell.
     * @param isSelected true if the cell is selected, otherwise false.
     * @param hasFocus   true if the cell has focus, otherwise false.
     * @param row        the row index of the cell.
     * @param column     the column index of the cell.
     * @return the rendered component.
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        if(value == null)
        {
            Component component = super.getTableCellRendererComponent(table, null, isSelected, false, row, column);
            component.setBackground(table.getBackground());
            component.setForeground(table.getForeground());
            return component;
        }

        Object[] objects = (Object[]) value;

        if(objects.length == 0)
        {
            Component component = super.getTableCellRendererComponent(table, null, isSelected, false, row, column);
            component.setBackground(table.getBackground());
            component.setForeground(table.getForeground());
            return component;
        }

        int current = ((table.getColumnCount() - 1) / Var.DAYS_PER_WEEK.length);
        boolean border = current != 0 && (column % current) == 0;

        Graphics g = getGraphics();
        if(g instanceof Graphics2D)
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        if(objects.length == 1)
        {
            if(objects[0] instanceof TimetableCell timetableCell)
            {
                Color background = timetableCell.getColor();

                Color foreground = getForeground(background);

                JLabel label = (JLabel) super.getTableCellRendererComponent(table,
                        timetableCell.isSameSubject() ? "" : timetableCell.getSubjectType(), isSelected, false, row,
                        column);
                label.setBackground(background);
                label.setForeground(foreground);

                JLabel bottomTeacher = new JLabel(timetableCell.isSameSubject() ? "" : timetableCell.getTeacherName());
                JLabel bottomRoom = new JLabel(timetableCell.isSameSubject() ? "" : timetableCell.getRoom());

                bottomTeacher.setBackground(background);
                bottomTeacher.setForeground(foreground);
                bottomRoom.setBackground(background);
                bottomRoom.setForeground(foreground);

                bottomTeacher.setHorizontalAlignment(CENTER);
                bottomRoom.setHorizontalAlignment(CENTER);

                JPanel panel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1.0;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.gridwidth = GridBagConstraints.REMAINDER;

                JPanel bottomPanel = new JPanel(new BorderLayout());

                if(timetableCell.subject != null)
                {
                    JLabel idLabel = new JLabel(timetableCell.subject.getId());
                    idLabel.setForeground(foreground);
                    idLabel.setHorizontalAlignment(CENTER);
                    bottomPanel.add(idLabel, BorderLayout.NORTH);
                }

                bottomPanel.add(bottomTeacher, BorderLayout.CENTER);
                bottomPanel.add(bottomRoom, BorderLayout.SOUTH);

                int marginSize = 5;
                bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, marginSize, 0, marginSize));

                bottomPanel.setBackground(background);
                panel.setBackground(background);

                panel.add(label, gbc);

                gbc.gridy = 1;
                panel.add(bottomPanel, gbc);

                if(border)
                    panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, table.getGridColor()));

                return panel;
            }

            Component component =
                    super.getTableCellRendererComponent(table, objects[0], isSelected, false, row, column);
            component.setBackground(table.getBackground());
            component.setForeground(table.getForeground());

            if(row == 0 || row == 1)
                component.setFont(new Font(component.getFont().getName(), Font.BOLD, 15));

            if(component instanceof JComponent jComponent)
            {
                jComponent.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
                return jComponent;
            }
            return component;
        }

        List<TimetableCell> list =
                Arrays.stream(objects).filter(o -> o instanceof TimetableCell).map(o -> (TimetableCell) o).toList();

        JPanel panel = new JPanel(new GridLayout())
        {
            @Override
            public void paintComponent(Graphics g)
            {
                setOpaque(false);

                int width = getWidth();
                int height = getHeight();

                Color sc = list.getFirst().getColor();
                Color ec = list.getLast().getColor();

                GradientPaint paint =
                        new GradientPaint(0, (float) height / 2, sc, width, (float) height / 2, ec, false);
                Graphics2D g2d = (Graphics2D) g;
                Paint oldPaint = g2d.getPaint();
                g2d.setPaint(paint);
                g2d.fillRect(0, 0, width, height);
                g2d.setPaint(oldPaint);

                super.paintComponent(g);
            }
        };

        for(int i = 0; i < objects.length; i++)
        {
            Object o = objects[i];
            if(!(o instanceof TimetableCell timetableCell))
                continue;

            Color background = timetableCell.getColor();

            Color foreground = getForeground(background);

            JLabel temp = (JLabel) super.getTableCellRendererComponent(table,
                    timetableCell.isSameSubject() ? "" : timetableCell.getSubjectType(), isSelected, false, row,
                    column);

            JLabel label = new JLabel(temp.getText());
            label.setHorizontalAlignment(CENTER);
            label.setBackground(background);
            label.setForeground(foreground);

            JPanel singlePanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridwidth = GridBagConstraints.REMAINDER;

            JPanel singleBottomPanel = new JPanel(new GridLayout(0, 1));

            JLabel bottomTeacher = new JLabel(timetableCell.isSameSubject() ? "" : timetableCell.getTeacherName());
            JLabel bottomRoom = new JLabel(timetableCell.isSameSubject() ? "" : timetableCell.getRoom());

            bottomTeacher.setBackground(background);
            bottomTeacher.setForeground(foreground);
            bottomRoom.setBackground(background);
            bottomRoom.setForeground(foreground);

            bottomTeacher.setHorizontalAlignment(CENTER);
            bottomRoom.setHorizontalAlignment(CENTER);

            if(timetableCell.subject != null)
            {
                JLabel idLabel = new JLabel(timetableCell.subject.getId());
                idLabel.setForeground(foreground);
                singleBottomPanel.add(idLabel);
            }

            singleBottomPanel.add(bottomTeacher);
            singleBottomPanel.add(bottomRoom);

            int marginSize = 5;
            singleBottomPanel.setBorder(BorderFactory.createEmptyBorder(0, marginSize, 0, marginSize));

            singleBottomPanel.setBackground(background);
            singlePanel.setBackground(background);

            singlePanel.add(label, gbc);

            gbc.gridy = 1;
            singlePanel.add(singleBottomPanel, gbc);

            if(i + 1 != objects.length)
                singlePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, table.getGridColor()));

            panel.add(singlePanel);
        }

        if(border)
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, table.getGridColor()));

        return panel;
    }

    /**
     * Calculates the foreground color based on the brightness of the background color.
     *
     * @param background the background color.
     * @return the foreground color.
     */
    public Color getForeground(Color background)
    {
        double backgroundBrightness = calculateBrightness(background);
        double threshold = 0.5;

        return backgroundBrightness < threshold ? Color.WHITE : Color.BLACK;
    }

    /**
     * Calculates the brightness of a color.
     *
     * @param color the color.
     * @return the brightness value (between 0 and 1).
     */
    public double calculateBrightness(Color color)
    {
        return (0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue()) / 255;
    }
}
