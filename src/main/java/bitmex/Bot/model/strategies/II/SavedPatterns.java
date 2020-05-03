package bitmex.Bot.model.strategies.II;

import bitmex.Bot.view.ConsoleHelper;
import bitmex.Bot.model.DatesTimes;


import java.io.Serializable;
import java.util.ArrayList;




public class SavedPatterns implements Serializable {
    private static ArrayList<ArrayList<String>> listsPricePatterns = new ArrayList<>();
    private static int maxArraySize = 0;


    public static ArrayList<ArrayList<String>> getListsPricePatterns() {
        return listsPricePatterns;
    }


    public static void addListsPricePatterns(ArrayList<String> listsPricePatterns) {
        SavedPatterns.isThereSuchCombination(listsPricePatterns);
    }


    // ищем есть ли такие патерны если нет то добавляем,
    // если есть то устанавливаем приоритет
    private static void isThereSuchCombination(ArrayList<String> arrayList) {
        ConsoleHelper.writeMessage("Сравниваю ПАТТЕРН с имеющимися ---- "
                + DatesTimes.getDateTerminal());

        // перебираем массив стратегий и сравниваем с пришедшим
        for (ArrayList<String> stringArrayList : SavedPatterns.listsPricePatterns) {
            boolean result = true;

            // проверяем совпадает ли их размер
            if (stringArrayList.size() == arrayList.size()) {

                // если размер совпал то начинаем сравнивать построчно
                // не считая 0-вой строки так как там инфа о паттерне
                for (int i = 1; i < arrayList.size(); i++) {
                    String[] arr1 = stringArrayList.get(i).split("\"preview\": \"");
                    String[] arr2 = arrayList.get(i).split("\"preview\": \"");
                    String[] strings1 = arr1[1].split("\"");
                    String[] strings2 = arr2[1].split("\"");

                    // если хоть один объект не равен то прирываем цикл
                    if (!strings1[0].equals(strings2[0])) {
                        result = false;
                        break;
                    }
                }

                // если цикл не прерван и флаг TRUE то корректируем инфо данные о патерне
                // с учетом информации пришедшего паттерна
                // а так же прекращаем процесс поиска и сравнения
                if (result) {
                    ConsoleHelper.writeMessage("ПАТТЕРН такой есть - обновляю информацию ---- "
                            + DatesTimes.getDateTerminal());
                    stringArrayList.add(0, SavedPatterns.setPriority(stringArrayList.get(0), arrayList.get(0)));
                    return;
                }
            }
        }

        // если совпадение не было найдено - добавляем данный патерн в массив
        ConsoleHelper.writeMessage("Такого ПАТТЕРНА нет - ДОБАВЛЕН ---- "
                + DatesTimes.getDateTerminal());
        SavedPatterns.listsPricePatterns.add(arrayList);
        maxArraySize = Math.max(arrayList.size(), maxArraySize);

    }


    // обновляем информационные данные в строке информации
    private static String setPriority(String s1, String s2) {
        // распарсили строки
        String[] strings1 = s1.split("===");
        String[] strings2 = s2.split("===");

        // спарсили числа и прибавили их
        int sell = Integer.parseInt(strings1[3]) + Integer.parseInt(strings2[3]);
        int buy = Integer.parseInt(strings1[1]) + Integer.parseInt(strings2[1]);
        // считаем среднюю цену отклонения в противоположную сторону
        double average = (Double.parseDouble(strings1[5]) + Double.parseDouble(strings2[5])) / 2;
        // обновляем максимальное отклонение
        double max = Math.max(Double.parseDouble(strings1[7]), Double.parseDouble(strings2[7]));

        // вернули итоговую инфо строку
        return  strings1[0] + "===" + buy + "===" + strings1[2] + "===" + sell + "===" + strings1[4] + "==="
                + average + "===" + strings1[6] + "===" + max + "===ID==="
                + ((int) (Math.round(Math.abs(Math.random() * 200 - 100)) * 39));
    }


    // находим и отдаем массивы нужной длины - размера
    public static ArrayList<ArrayList<String>> getListFoSize(int size) {
        ArrayList<ArrayList<String>> listFoSize = new ArrayList<>();

        for (ArrayList<String> list : listsPricePatterns) {
            if (list.size() == size) {
                listFoSize.add(list);
            }
        }
        if (listFoSize.size() > 0) return listFoSize;
        else return null;
    }



    // возвращаем самую большую длину имеющегося паттерна
    public static int getMaxArraySize() {
        return maxArraySize;
    }
}
