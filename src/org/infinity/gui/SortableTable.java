// Near Infinity - An Infinity Engine Browser and Editor
// Copyright (C) 2001 - 2018 Jon Olav Hauglid
// See LICENSE.txt for license information

package org.infinity.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.infinity.icon.Icons;
import org.infinity.resource.Profile;

public final class SortableTable extends JTable implements MouseListener
{
  private final SortableTableModel tableModel;
  private boolean sortAscending;
  private int sortByColumn;

  public SortableTable(String[] columnNames, Class<?>[] columnClasses, Integer[] columnWidths)
  {
    tableModel = new SortableTableModel(columnNames, columnClasses);
    setModel(tableModel);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setDefaultRenderer(Object.class, new ToolTipTableCellRenderer());
    getTableHeader().setDefaultRenderer(new TableHeaderRenderer());
    getTableHeader().addMouseListener(this);
    for (int i = 0; i < columnWidths.length; i++)
      getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
  }

  public void addTableItem(TableItem item)
  {
    tableModel.addTableItem(item);
  }

  public void clear()
  {
    tableModel.clear();
  }

  public TableItem getTableItemAt(int rowIndex)
  {
    return tableModel.getTableItemAt(rowIndex);
  }

  public void tableComplete()
  {
    tableModel.sort();
  }

  public void tableComplete(int sortByColumn)
  {
    this.sortByColumn = sortByColumn;
    tableModel.sort();
  }


  public void saveCheckResult(Component parent, String header) {
    saveResult(parent, "Save check result", header);
  }
  public void saveSearchResult(Component parent, String query) {
    saveResult(parent, "Save search result", "Searched for: " + query);
  }
  private void saveResult(Component parent, String dialogTitle, String header) {
    final JFileChooser chooser = new JFileChooser(Profile.getGameRoot().toFile());
    chooser.setDialogTitle(dialogTitle);
    chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), "result.txt"));
    if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
      final Path output = chooser.getSelectedFile().toPath();
      if (Files.exists(output)) {
        final String[] options = {"Overwrite", "Cancel"};
        if (JOptionPane.showOptionDialog(parent, output + " exists. Overwrite?",
                                         dialogTitle, JOptionPane.YES_NO_OPTION,
                                         JOptionPane.WARNING_MESSAGE, null, options, options[0]) != 0) {
          return;
        }
      }
      try (final BufferedWriter bw = Files.newBufferedWriter(output)) {
        bw.write(header); bw.newLine();
        bw.write("Number of hits: " + getRowCount()); bw.newLine();
        for (int i = 0; i < getRowCount(); i++) {
          bw.write(getTableItemAt(i).toString()); bw.newLine();
        }
        JOptionPane.showMessageDialog(parent, "Result saved to " + output,
                                      "Save complete", JOptionPane.INFORMATION_MESSAGE);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(parent, "Error while saving " + output + " (details in the trace)",
                                      "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
      }
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    final int viewColumn = getColumnModel().getColumnIndexAtX(e.getX());
    final int column = convertColumnIndexToModel(viewColumn);
    if (column == sortByColumn)
      sortAscending = !sortAscending;
    else {
      sortByColumn = column;
      sortAscending = false;
    }
    getTableHeader().repaint();
    tableModel.sort();
  }

  @Override
  public void mousePressed(MouseEvent e) {}

  @Override
  public void mouseReleased(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

// -------------------------- INNER CLASSES --------------------------

  private final class TableHeaderRenderer extends DefaultTableCellRenderer
  {
    private TableHeaderRenderer()
    {
      setHorizontalTextPosition(DefaultTableCellRenderer.LEFT);
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column)
    {
      if (table != null) {
        JTableHeader header = table.getTableHeader();
        if (header != null) {
          setForeground(header.getForeground());
          setBackground(header.getBackground());
          setFont(header.getFont());
          setText(' ' + table.getModel().getColumnName(column));
        }
      }

      if (sortByColumn == column)
        setIcon(sortAscending ? Icons.getIcon(Icons.ICON_UP_16) : Icons.getIcon(Icons.ICON_DOWN_16));
      else
        setIcon(null);
      return this;
    }
  }

  private final class SortableTableModel implements TableModel, Comparator<TableItem>
  {
    private final List<TableModelListener> listeners = new ArrayList<>();
    private final List<TableItem> tableItems = new ArrayList<>();
    private final Class<?>[] columnClasses;
    private final String[] columnNames;

    private SortableTableModel(String[] columnNames, Class<?>[] columnClasses)
    {
      this.columnNames   = columnNames   != null ? columnNames   : new String[0];
      this.columnClasses = columnClasses != null ? columnClasses : new Class<?>[0];
    }

    private void addTableItem(TableItem item)
    {
      tableItems.add(item);
    }

    private TableItem getTableItemAt(int rowIndex)
    {
      return tableItems.get(rowIndex);
    }

    private void fireTableChangedEvent()
    {
      final TableModelEvent event = new TableModelEvent(this);
      for (TableModelListener l : listeners) {
        l.tableChanged(event);
      }
    }

    public void sort()
    {
      Collections.sort(tableItems, this);
      fireTableChangedEvent();
    }

    private void clear()
    {
      tableItems.clear();
      fireTableChangedEvent();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return columnClasses[columnIndex];
    }

    @Override
    public int getColumnCount()
    {
      return columnClasses.length;
    }

    @Override
    public String getColumnName(int columnIndex)
    {
      return columnNames[columnIndex];
    }

    @Override
    public int getRowCount()
    {
      return tableItems.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      return tableItems.get(rowIndex).getObjectAt(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return false;
    }

    @Override
    public void addTableModelListener(TableModelListener l)
    {
      listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l)
    {
      listeners.remove(l);
    }

    @Override
    public int compare(TableItem o1, TableItem o2)
    {
      int res;
      if (getColumnClass(sortByColumn) == Integer.class) {
        Integer int1 = (Integer)o1.getObjectAt(sortByColumn);
        Integer int2 = (Integer)o2.getObjectAt(sortByColumn);
        res = int1.compareTo(int2);
      }
      else {
        Object item1 = o1.getObjectAt(sortByColumn);
        Object item2 = o2.getObjectAt(sortByColumn);
        String string1 = (item1 != null) ? item1.toString() : "";
        String string2 = (item2 != null) ? item2.toString() : "";
        res = string1.compareToIgnoreCase(string2);
      }
      if (sortAscending) {
        res = -res;
      }
      return res;
    }
  }
}

