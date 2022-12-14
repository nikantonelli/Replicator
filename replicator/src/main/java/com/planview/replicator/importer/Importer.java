package com.planview.replicator.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONObject;

import com.planview.replicator.ChangesColumns;
import com.planview.replicator.ColNames;
import com.planview.replicator.Debug;
import com.planview.replicator.InternalConfig;
import com.planview.replicator.LkUtils;
import com.planview.replicator.SupportedXlsxFields;
import com.planview.replicator.XlUtils;
import com.planview.replicator.leankit.AccessCache;
import com.planview.replicator.leankit.Board;
import com.planview.replicator.leankit.BoardUser;
import com.planview.replicator.leankit.Card;
import com.planview.replicator.leankit.CustomField;
import com.planview.replicator.leankit.CustomIcon;
import com.planview.replicator.leankit.Lane;

public class Importer {
	Debug d = new Debug();

	InternalConfig cfg = null;

	public Importer(InternalConfig config) {
		cfg = config;
		d.setLevel(cfg.debugLevel);
		XlUtils.d.setLevel(cfg.debugLevel);
	}

	public void go() {

		cfg.cache = new AccessCache(cfg, cfg.destination);

		d.p(Debug.ALWAYS, "Starting Import at: %s\n", new Date());
		/**
		 * cfg might contain the sheet info for the importer if it came from the
		 * exporter directly
		 */

		cfg.changesSheet = cfg.wb
				.getSheet(XlUtils.validateSheetName(InternalConfig.CHANGES_SHEET_NAME + cfg.source.BoardName));

		//Check for source information
		if (null == cfg.changesSheet) {
			d.p(Debug.ERROR, "Cannot find required Changes sheet in file: \"%s\" Have you done the export?\n", cfg.xlsxfn);
			System.exit(1);
		}

		//Check for destination information - i.e. does destination exist?
		Board brd = LkUtils.getBoardByTitle(cfg, cfg.destination);
		if (brd == null) {
			d.p(Debug.ERROR, "Cannot find required destination board shown in file: \"%s\" Do you need -r (remake) option?\n", cfg.destination.BoardName);
			System.exit(1);
		}
		ChangesColumns cc = XlUtils.checkChangeSheetColumns(cfg.changesSheet);
		if (cc == null) {
			System.exit(1);
		}

		// Find all the change records for today
		Iterator<Row> row = cfg.changesSheet.iterator();
		ArrayList<Row> todaysChanges = new ArrayList<Row>();

		// Nw add the rows of today to an array so we can give an info message

		row.next(); // Skip first row with headers
		while (row.hasNext()) {

			Row tr = row.next();
			if (tr.getCell(cc.group) != null) {
				if (tr.getCell(cc.group).getNumericCellValue() == cfg.group) {
					todaysChanges.add(tr);
				}
			}
		}

		if (todaysChanges.size() == 0) {
			d.p(Debug.INFO, "No actions to take for group %d\n", cfg.group);
			return;
		} else {
			d.p(Debug.INFO, "%d actions to take for group %d on board \"%s\"\n", todaysChanges.size(), cfg.group,
					cfg.source.BoardName);
		}
		// Now scan through the changes doing the actions
		Iterator<Row> cItor = todaysChanges.iterator();
		Row item = null;
		while (cItor.hasNext()) {
			Row change = cItor.next();
			// Get the item that this change refers to
			// First check the validity of the info
			if ((change.getCell(cc.row) == null) || (change.getCell(cc.action) == null)) {
				d.p(Debug.WARN, "Cannot decode change info in row \"%d\" - skipping\n", change.getRowNum());
				continue;
			}

			// In case the changes get repositioned, we use the formula to keep track of
			// where the row 'actually' is
			// Excel updates the formula for us if the user edits the changes
			String cf = change.getCell(cc.row).getCellFormula();
			CellReference ca = new CellReference(cf);
			XSSFSheet iSht = cfg.wb.getSheet(ca.getSheetName());
			item = iSht.getRow(ca.getRow());

			Integer idCol = XlUtils.findColumnFromSheet(iSht, ColNames.ID);
			Integer titleCol = XlUtils.findColumnFromSheet(iSht, "title");
			Integer typeCol = XlUtils.findColumnFromSheet(iSht, "type");

			if ((idCol == null) || (titleCol == null)) {
				d.p(Debug.WARN, "Cannot locate \"ID\" and \"title\" columns needed in sheet \"%s\" - skipping\n",
						iSht.getSheetName());
				continue;
			}

			// Check title is present for a Create
			if ((change.getCell(cc.action).getStringCellValue().equals("Create"))
					&& ((item.getCell(titleCol) == null) || (item.getCell(titleCol).getStringCellValue().isEmpty()))) {
				d.p(Debug.WARN,
						"Required \"title\" column/data missing in sheet \"%s\", row: %d for a Create - skipping\n",
						iSht.getSheetName(), item.getRowNum());
				continue;
			}

			if ((typeCol == null) || (item.getCell(typeCol) == null)) {
				d.p(Debug.WARN, "Cannot locate \"type\" column on row:  %d  - using default for board\n",
						item.getRowNum());
			}
			String field = change.getCell(cc.field).getStringCellValue();
			// Check import requirements against command line
			if (change.getCell(cc.action).getStringCellValue() == "Modify") {
				if (((field == "attachments") && !cfg.exportAttachments)
						|| ((field == "Task") && !cfg.exportTasks)
						|| ((field == "comments") && !cfg.exportComments)) {
					d.p(Debug.WARN, "Ignoring action \"%s\" on item \"%s\", not set to import %s\n",
							change.getCell(cc.action).getStringCellValue(), item.getCell(titleCol).getStringCellValue(),
							field);
					continue; // Break out and try next change
				}
			}
			String id = null;
			boolean runAction = XlUtils.notIgnoreType(cfg, item.getCell(typeCol).getStringCellValue());
			String action = change.getCell(cc.action).getStringCellValue();
			if (action.equals("Create")) {

				if (runAction) {
					id = doAction(change, item);
					if (id != null) {
						if (item.getCell(idCol) == null) {
							item.createCell(idCol);
						}
						item.getCell(idCol).setCellValue(id);
						d.p(Debug.INFO, "Create card \"%s\" (changes row %s)\n", id, change.getRowNum());
					}
				} else {
					id = "Skipping";
					d.p(Debug.INFO, "Skipping action \"%s\" for card \"%s\" (changes row %s)\n",
							action, item.getCell(titleCol).getStringCellValue(), change.getRowNum());
				}
			} else {
				id = doAction(change, item);
				if (id != null) {
					d.p(Debug.INFO, "Mod: \"%s\" on card \"%s\" (changes row %s)\n",
							field, id, change.getRowNum());
				}
			}
			if (id != null) {
				XlUtils.writeFile(cfg, cfg.xlsxfn, cfg.wb);
			} else {
				d.p(Debug.ERROR, "%s",
						"Got null back from doAction(). Most likely card deleted!\n");
			}
		}
		XSSFFormulaEvaluator.evaluateAllFormulaCells(cfg.wb);
	}

	private String doAction(Row change, Row item) {

		ChangesColumns cc = XlUtils.checkChangeSheetColumns(cfg.changesSheet);
		String cf = change.getCell(cc.row).getCellFormula();
		CellReference ca = new CellReference(cf);
		XSSFSheet iSht = cfg.wb.getSheet(ca.getSheetName());

		/**
		 * We need to get the header row for this sheet and work out which columns the
		 * fields are in. It is possible that fields could be different between sheets,
		 * so we have to do this every 'change'
		 */

		Iterator<Row> iRow = iSht.iterator();
		Row iFirst = iRow.next();
		/**
		 * Now iterate across the cells finding out which fields need to be set
		 */
		JSONObject fieldLst = new JSONObject();
		Iterator<Cell> cItor = iFirst.iterator();
		Integer idCol = null;
		while (cItor.hasNext()) {
			Cell cl = cItor.next();
			String nm = cl.getStringCellValue();
			if (nm.equals(ColNames.ID)) {
				idCol = cl.getColumnIndex();
				continue;
			} else if (nm.equals(ColNames.SOURCE_ID)) {
				continue;
			}
			fieldLst.put(nm, cl.getColumnIndex());
		}

		if (change.getCell(cc.action).getStringCellValue().equalsIgnoreCase("Create")) {
			// Now 'translate' the spreadsheet name:col pairs to fieldName:value pairs

			JSONObject flds = XlUtils.jsonCardFromRow(cfg, cfg.destination, fieldLst, item, null);

			// We need to find the ID of the board that this is targetting for a card
			// creation
			if (!fieldLst.has("boardId")) {
				Board brd = LkUtils.getBoardByTitle(cfg, cfg.destination);
				if (brd != null)
					flds.put("boardId", brd.id);
				else {
					d.p(Debug.ERROR, "Could not create card on board \"%s\" with details: \"%s\"\n",
							cfg.destination.BoardName,
							flds.toString());
					System.exit(18);
				}
			} else {
				flds.put("boardId", XlUtils.getCell(item, fieldLst.getInt("boardId")));
			}
			Card card = LkUtils.createCard(cfg, cfg.destination, flds); // Change from human readable to API fields on
			// the way
			if (card == null) {
				d.p(Debug.ERROR, "Could not create card on board \"%s\" with details: \"%s\"\n", flds.get("boardId"),
						flds.toString());
				System.exit(16);
			}
			return card.id;

		} else if (change.getCell(cc.action).getStringCellValue().equalsIgnoreCase("Modify")) {
			// Scan for the item and then fetch that card
			// Card card = LkUtils.getCard(cfg, cfg.destination,
			// item.getCell(idCol).getStringCellValue());
			Cell colcell = change.getCell(cc.row);
			String cellval = null;
			switch (colcell.getCellType()) {
				case FORMULA: {
					String ccf = change.getCell(cc.row).getCellFormula();
					CellReference cca = new CellReference(ccf);
					XSSFSheet cSheet = cfg.wb.getSheet(cca.getSheetName());
					Row target = cSheet.getRow(cca.getRow());
					cellval = target.getCell(cca.getCol()).getStringCellValue();
					break;
				}
				case STRING: {
					cellval = colcell.getStringCellValue();
					break;
				}
				default: {
					break;
				}
			}
			Card card = XlUtils.findCardByTitle(cfg, cellval);

			Card newCard = null;

			if (card == null) {
				d.p(Debug.ERROR, "Could not locate \"single\" card with title: \"%s\"\n",
						item.getCell(XlUtils.findColumnFromName(iFirst, ColNames.TITLE)).getStringCellValue());
			} else {
				// Don't need this when modifying an existing item.
				if (fieldLst.has("boardId")) {
					fieldLst.remove("boardId");
				}
				JSONObject fld = new JSONObject();
				JSONObject vals = new JSONObject();

				String field = change.getCell(cc.field).getStringCellValue();
				SupportedXlsxFields allFields = new SupportedXlsxFields();

				try {

					// If its part of the fields we don't want, then ignore
					(allFields.new ReadOnly()).getClass().getField(field);
				} catch (NoSuchFieldException e) {

					switch (field) {
						case "Task": {
							// Get row for the task
							String tcf = change.getCell(cc.value).getCellFormula();
							CellReference tca = new CellReference(tcf);
							XSSFSheet cSheet = cfg.wb.getSheet(tca.getSheetName());
							Row task = cSheet.getRow(tca.getRow());

							JSONObject jsonTask = XlUtils.jsonCardFromRow(cfg, cfg.destination, fieldLst, task,
									card.id);
							if (task.getCell(idCol) == null) {

								task.createCell(idCol);
							}
							task.getCell(idCol)
									.setCellValue(LkUtils.addTask(cfg, cfg.destination, card.id, jsonTask).id);
							break;
						}
						case ColNames.ASSIGNED_USERS: {
							/**
							 * We need to try and match the email address in the destination and fetch the
							 * userID
							 */
							String usersList = change.getCell(cc.value).getStringCellValue();
							if (usersList != null) {
								ArrayList<BoardUser> boardUsers = LkUtils.getUsers(cfg, cfg.destination); // Fetch the
																											// board
																											// users
								if (boardUsers != null) {

									if (usersList != null) {
										String[] users = usersList.split(",");
										ArrayList<String> usersToPut = new ArrayList<>();
										for (int i = 0; i < users.length; i++) {

											// Check if they are a board user so we don't error.
											for (int j = 0; j < boardUsers.size(); j++) {
												if (users[i].equals(boardUsers.get(j).emailAddress)) {
													usersToPut.add(boardUsers.get(j).userId);
												}
											}
										}
										vals.put("value",usersToPut.toArray());
										fld.put("assignedUserIds", vals);
									}
								}
							}
							break;
						}
						case ColNames.CUSTOM_ICON: {
							// Incoming customIcon value is a name. We need to translate to
							// an id
							Cell cstmcell = change.getCell(cc.value);
							String cstmval = null;
							switch (cstmcell.getCellType()) {
								case FORMULA: {
									String ccf = cstmcell.getCellFormula();
									CellReference cca = new CellReference(ccf);
									XSSFSheet cSheet = cfg.wb.getSheet(cca.getSheetName());
									Row target = cSheet.getRow(cca.getRow());
									cstmval = target.getCell(cca.getCol()).getStringCellValue();
									break;
								}
								case STRING: {
									cstmval = cstmcell.getStringCellValue();
									break;
								}
								default: {
									break;
								}
							}
							CustomIcon ci = LkUtils.getCustomIcon(cfg, cfg.destination, cstmval);
							if (ci != null) {
								vals.put("value", ci.id);
								fld.put("customIconId", vals);
							}
							break;
						}
						case ColNames.LANE: {
							String[] bits = ((String) XlUtils.getCell(change, cc.value)).split(",");
							Lane foundLane = LkUtils.getLaneFromBoardTitle(cfg, cfg.destination,
									cfg.destination.BoardName,
									bits[0]);
							if (foundLane != null) {
								vals.put("value", foundLane.id);
								if (bits.length > 1) {
									vals.put("value2", bits[1]);
								}
								fld.put("Lane", vals);
							}
							break;
						}

						case "Parent": {
							// Get the parentID originally associated with this card
							String parentId = change.getCell(cc.value).getStringCellValue();
							// Find the row with that ID in it
							Card crd = XlUtils.findCardByTitle(cfg, parentId);
							if ((crd != null) && (crd.id != null)) {
								vals.put("value", crd.id);
								fld.put(field, vals);
							}
							break;

						}

						default: {
							// Check if this is a standard/custom field and redo the 'put'

							CustomField ctmf = LkUtils.getCustomField(cfg, cfg.destination, field);
							if (ctmf != null) {
								vals.put("value", field);
								vals.put("value2", XlUtils.getCell(change, cc.value));
								fld.put("CustomField", vals);
							} else {
								vals.put("value", XlUtils.getCell(change, cc.value));
								fld.put(field, vals);
							}

							break;
						}
					}
				}

				newCard = LkUtils.updateCard(cfg, cfg.destination, card.id, fld);
				if (newCard == null) {
					d.p(Debug.ERROR, "Could not modify card \"%s\" on board %s with details: %s", card.id,
							cfg.destination.BoardName, fld.toString());
					System.exit(17);
				}
				return card.id;
			}
		}
		// Unknown option comes here
		return null;
	}

}
