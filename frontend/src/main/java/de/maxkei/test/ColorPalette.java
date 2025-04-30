package de.maxkei.test;

import com.formdev.flatlaf.FlatDarculaLaf;
import de.maxkei.enums.SubjectType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ColorPalette
{
    /**
     * Main function to display a color palette JFrame with different subject labels.
     *
     * @param args the command-line arguments passed to the program
     */
    public static void main(String[] args)
    {
        Font font = new JLabel().getFont();

        FlatDarculaLaf.setup();

        JFrame frame = new JFrame("Color Palette");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(600, 450));

        JPanel panel = new JPanel(new GridLayout());

        for(SubjectType subjectType : SubjectType.values())
        {
            JPanel labelPanel = new JPanel();

            JLabel label = new JLabel(subjectType.getName());
            label.setForeground(Color.BLACK);
            label.setFont(font);

            labelPanel.setBackground(subjectType.getColor());
            labelPanel.add(label);

            labelPanel.setTransferHandler(new PanelTransferHandler());
            labelPanel.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    JComponent c = (JComponent) e.getSource();
                    TransferHandler handler = c.getTransferHandler();
                    handler.exportAsDrag(c, e, TransferHandler.MOVE);
                }
            });

            panel.add(labelPanel);
        }

        frame.add(panel);

        frame.setVisible(true);
    }

    /**
     * TransferHandler to manage drag-and-drop functionality for panels.
     */
    private static class PanelTransferHandler extends TransferHandler
    {
        /**
         * Returns the type of transfer actions supported by the source component.
         *
         * @param c the source component
         * @return the transfer actions (in this case, MOVE)
         */
        @Override
        public int getSourceActions(JComponent c)
        {
            return MOVE;
        }

        /**
         * Creates a Transferable to encapsulate the data to be transferred during a drag-and-drop operation.
         *
         * @param c the component holding the data to be transferred
         * @return the Transferable object containing the transfer data
         */
        @Override
        protected Transferable createTransferable(JComponent c)
        {
            return new TransferablePanel(c);
        }

        /**
         * Determines whether the data being dragged can be imported into the target component.
         *
         * @param support the TransferSupport object containing details of the drop location
         * @return true if the drop data is a JPanel and supports the appropriate DataFlavor, false otherwise
         */
        @Override
        public boolean canImport(TransferHandler.TransferSupport support)
        {
            return support.getComponent() instanceof JPanel &&
                    support.isDataFlavorSupported(PanelDataFlavor.PANEL_DATA_FLAVOR);
        }

        /**
         * Handles the actual data import for the drop operation.
         *
         * @param support the TransferSupport object containing details of the drop location
         * @return true if the data was successfully imported, false otherwise
         */
        @Override
        public boolean importData(TransferHandler.TransferSupport support)
        {
            if(!canImport(support))
                return false;

            try
            {
                JPanel targetPanel = (JPanel) support.getComponent();
                JPanel draggedPanel =
                        (JPanel) support.getTransferable().getTransferData(PanelDataFlavor.PANEL_DATA_FLAVOR);

                Container parent = targetPanel.getParent();
                int targetIndex = getIndex(parent, targetPanel);
                int draggedIndex = getIndex(parent, draggedPanel);

                parent.remove(draggedPanel);
                parent.add(draggedPanel, targetIndex);
                parent.remove(targetPanel);
                parent.add(targetPanel, draggedIndex);
                parent.revalidate();
                parent.repaint();
                return true;
            } catch(Exception ignored)
            {
            }
            return false;
        }

        /**
         * Finds the index of a child component within its parent container.
         *
         * @param parent the parent container
         * @param child  the child component
         * @return the index of the child component within the parent, or -1 if not found
         */
        private int getIndex(Container parent, Component child)
        {
            for(int i = 0; i < parent.getComponentCount(); i++)
            {
                if(parent.getComponent(i) == child)
                    return i;
            }
            return -1;
        }
    }

    /**
     * Transferable implementation for transferring panels during drag-and-drop.
     */
    private record TransferablePanel(JComponent panel) implements Transferable
    {
        /**
         * Returns the data flavors supported by this Transferable.
         *
         * @return an array of supported DataFlavor objects
         */
        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[]{PanelDataFlavor.PANEL_DATA_FLAVOR};
        }

        /**
         * Checks if the specified data flavor is supported by this Transferable.
         *
         * @param flavor the data flavor to check
         * @return true if the flavor is supported, false otherwise
         */
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return flavor.equals(PanelDataFlavor.PANEL_DATA_FLAVOR);
        }

        /**
         * Returns the transfer data for the specified data flavor.
         *
         * @param flavor the data flavor for which the data is requested
         * @return the transfer data (in this case, the panel)
         */
        @Override
        public @NotNull Object getTransferData(DataFlavor flavor)
        {
            return panel;
        }
    }

    /**
     * Custom DataFlavor for identifying panel data during drag-and-drop.
     */
    public static class PanelDataFlavor extends DataFlavor
    {
        /**
         * The static instance of the custom DataFlavor.
         */
        public static final DataFlavor PANEL_DATA_FLAVOR;

        static
        {
            try
            {
                PANEL_DATA_FLAVOR = new PanelDataFlavor();
            } catch(ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * Constructs a new PanelDataFlavor.
         *
         * @throws ClassNotFoundException if the class for the DataFlavor cannot be found
         */
        public PanelDataFlavor() throws ClassNotFoundException
        {
            super(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + JComponent.class.getName());
        }
    }
}
