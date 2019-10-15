package Ciscos.delta.view;

import Ciscos.delta.model.Cisco;
import Ciscos.delta.model.Mac;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelOut {

    public ExcelOut(Cisco cisco) {
//        Cisco cisco1 = cisco;
        writeSheet(cisco);
    }

    private void writeSheet(Cisco cisco) {
        HSSFWorkbook writeWb = new HSSFWorkbook();
        Sheet sheet = writeWb.createSheet();
        int numRow = 0;
//                    int cellType = cell.getCellType(); //case Cell.CELL_TYPE_STRING: CELL_TYPE_NUMERIC: CELL_TYPE_FORMULA:
        HashMap<String, String> macIface = cisco.getMacIface();
        HashMap<String, String> macIp = Mac.getArpMac();

        Row writeRow = sheet.createRow(numRow);
        Cell writeCell = writeRow.createCell(0);
        writeCell.setCellValue(cisco.getName());
        writeCell = writeRow.createCell(2);
        writeCell.setCellValue(cisco.getIp());
        numRow = 2;

        numRow = getNumRow(cisco, sheet, numRow);
        numRow++;
        for (Map.Entry<String, String> entry : macIface.entrySet()) {
            writeRow = sheet.createRow(numRow);
            writeCell = writeRow.createCell(0);
            writeCell.setCellValue(entry.getValue());
            writeCell = writeRow.createCell(1);
            writeCell.setCellValue(entry.getKey());
            if (macIp.get(entry.getKey()) != null) {
                writeCell = writeRow.createCell(2);
                writeCell.setCellValue(macIp.get(entry.getKey()).split(" ")[0]);
                writeCell = writeRow.createCell(3);
                writeCell.setCellValue(macIp.get(entry.getKey()).split(" ")[1]);
            }
            numRow++;
        }
        try {
            writeWb.write(new FileOutputStream(cisco.getName() + ".xls"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getNumRow(Cisco cisco, Sheet sheet, int numRow) {
        Row writeRow;
        Cell writeCell;
        ArrayList<String> uniqueList = cisco.getUniqueList();
        for (String s : uniqueList) {
            String[] iface = s.split("\t");
            writeRow = sheet.createRow(numRow);
            for (int i = 0; i < 4; i++) {
                writeCell = writeRow.createCell(i);
                writeCell.setCellValue(iface[i]);
            }
            numRow++;
        }
        return numRow;
    }

    public static void writeUniqueSheet(List<Cisco> ciscos) {
        int numRow = 0;
        HSSFWorkbook writeWb = new HSSFWorkbook();
        Sheet sheet = writeWb.createSheet();
        Row writeRow;
        Cell writeCell;
        for (Cisco cisco : ciscos) {
            writeRow = sheet.createRow(numRow);
            writeCell = writeRow.createCell(0);
            writeCell.setCellValue(cisco.getName());
            writeCell = writeRow.createCell(2);
            writeCell.setCellValue(cisco.getIp());
            numRow += 2;
            numRow = getNumRow(cisco, sheet, numRow);
            numRow++;
        }
        try {
            writeWb.write(new FileOutputStream("uniq.xls"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
