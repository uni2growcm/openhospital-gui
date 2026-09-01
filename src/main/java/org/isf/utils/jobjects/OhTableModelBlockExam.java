package org.isf.utils.jobjects;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.isf.exa.model.Block;
import org.isf.generaldata.MessageBundle;

public class OhTableModelBlockExam implements TableModel {

	private final List<Block> dataListExam;
	private List<Block> filteredList;

	public OhTableModelBlockExam(ArrayList<Block> dataList) {
		this.dataListExam = dataList;
		this.filteredList = new ArrayList<>(dataList);
	}

	public int filter(String searchQuery) {
		this.filteredList = new ArrayList<>();
		for (Block block : this.dataListExam) {
			String strItem = block.getSearchString();
			if (strItem.indexOf(searchQuery.toLowerCase()) >= 0) {
				filteredList.add(block);
			}
		}
		return filteredList.size();
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return switch (columnIndex) {
			case 0 -> MessageBundle.getMessage("angal.common.code.txt");
			default -> MessageBundle.getMessage("angal.common.description.txt");
		};
	}

	@Override
	public int getRowCount() {
		if (this.filteredList == null) {
			return 0;
		}
		return this.filteredList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= 0 && rowIndex < this.filteredList.size()) {
			Block block = this.filteredList.get(rowIndex);
			if (columnIndex == 0) {
				return block.getCode();
			} else {
				return block.getDescription();
			}
		}
		return "";
	}

	public Block getObjectAt(int rowIndex) {
		if (rowIndex >= 0 && rowIndex < this.filteredList.size()) {
			return this.filteredList.get(rowIndex);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}
}