/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2026 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.utils.jobjects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.isf.utils.pagination.PageInfo;

/**
 * Reusable pagination navigation control: first / previous / page-select / next / last.
 * Screens own the actual data fetch; this panel only reports the page the user asked
 * for via {@link PageChangeListener} and reflects the resulting {@link PageInfo} back.
 *
 * @author Donfack Duval
 */
public class PaginationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public interface PageChangeListener {

		void onPageChange(int page);
	}

	private final PageChangeListener listener;
	private final JButton firstPageButton;
	private final JButton prevPageButton;
	private final JComboBox<Integer> pageSelect;
	private final JButton nextPageButton;
	private final JButton lastPageButton;

	private boolean updatingProgrammatically;
	private PageInfo lastPageInfo;

	public PaginationPanel(PageChangeListener listener) {
		this.listener = listener;

		firstPageButton = new JButton("<<");
		firstPageButton.setEnabled(false);
		firstPageButton.addActionListener(actionEvent -> fireIfPageInfoAvailable(0));

		prevPageButton = new JButton("<");
		prevPageButton.setEnabled(false);
		prevPageButton.addActionListener(actionEvent -> fireIfPageInfoAvailable(lastPageInfo.getPage() - 1));

		pageSelect = new JComboBox<>();
		pageSelect.setEnabled(false);
		pageSelect.addActionListener(actionEvent -> {
			if (updatingProgrammatically || pageSelect.getSelectedItem() == null) {
				return;
			}
			int selectedPage = (Integer) pageSelect.getSelectedItem() - 1;
			if (lastPageInfo == null || selectedPage != lastPageInfo.getPage()) {
				listener.onPageChange(selectedPage);
			}
		});

		nextPageButton = new JButton(">");
		nextPageButton.setEnabled(false);
		nextPageButton.addActionListener(actionEvent -> fireIfPageInfoAvailable(lastPageInfo.getPage() + 1));

		lastPageButton = new JButton(">>");
		lastPageButton.setEnabled(false);
		lastPageButton.addActionListener(actionEvent -> fireIfPageInfoAvailable(lastPageInfo.getTotalPages() - 1));

		add(firstPageButton);
		add(prevPageButton);
		add(pageSelect);
		add(nextPageButton);
		add(lastPageButton);
	}

	private void fireIfPageInfoAvailable(int page) {
		if (lastPageInfo != null) {
			listener.onPageChange(page);
		}
	}

	/**
	 * Refreshes the controls from the latest fetch. Pass {@code null} when there is no
	 * result set (empty search, error) to disable every control and clear the page list.
	 */
	public void update(PageInfo pageInfo) {
		boolean totalPagesChanged = pageInfo != null
				&& (lastPageInfo == null || lastPageInfo.getTotalPages() != pageInfo.getTotalPages());
		lastPageInfo = pageInfo;

		updatingProgrammatically = true;
		try {
			if (pageInfo == null) {
				pageSelect.removeAllItems();
				pageSelect.setEnabled(false);
				firstPageButton.setEnabled(false);
				prevPageButton.setEnabled(false);
				nextPageButton.setEnabled(false);
				lastPageButton.setEnabled(false);
				return;
			}

			if (totalPagesChanged) {
				pageSelect.removeAllItems();
				int totalPages = Math.max(1, pageInfo.getTotalPages());
				for (int page = 1; page <= totalPages; page++) {
					pageSelect.addItem(page);
				}
			}
			pageSelect.setSelectedItem(pageInfo.getPage() + 1);
			pageSelect.setEnabled(true);

			firstPageButton.setEnabled(pageInfo.isHasPreviousPage());
			prevPageButton.setEnabled(pageInfo.isHasPreviousPage());
			nextPageButton.setEnabled(pageInfo.isHasNextPage());
			lastPageButton.setEnabled(pageInfo.isHasNextPage());
		} finally {
			updatingProgrammatically = false;
		}
	}
}
