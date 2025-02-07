package org.isf.exa.gui;

import org.isf.exa.model.ExamTarget;
import org.isf.generaldata.MessageBundle;

public class ExamTargetItem {
		public final ExamTarget item;

		public ExamTargetItem(ExamTarget item) {
			this.item = item;
		}

		public ExamTarget getItem() {
			return item;
		}

		public static ExamTargetItem from(ExamTarget item) {
			return new ExamTargetItem(item);
		}

		public static ExamTargetItem from(String value) {
			ExamTarget item = null;
			if (value == null || value.isBlank()) {
				item = ExamTarget.no;
			}

			char first = value.charAt(0);

			item = switch (first) {
				case '1' -> ExamTarget.no;
				case '2' -> ExamTarget.prenatal;
				case '3' -> ExamTarget.postnatal;
				case '4' -> ExamTarget.both;
				default -> ExamTarget.no;
			};

			return ExamTargetItem.from(item);
		}
		
		@Override
		public boolean equals(Object other) {
		    if (other == null || getClass() != other.getClass()) {
		       return false;
		    }
		    return item.equals(((ExamTargetItem)other).item);
		}
		
		@Override
		public String toString() {
			String prefix = switch (item) {
			    case no -> "1 - ";
			    case prenatal -> "2 - ";
			    case postnatal -> "3 - ";
			    case both -> "4 - ";
			    default -> "1 - ";
			};
			
			return prefix + MessageBundle.getMessage("angal.exa.examtarget." + item.toString() + ".txt");
		}
	}
