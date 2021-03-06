package bitmex.Bot.model.strategies.IIUser;

import bitmex.Bot.model.StringHelper;
import bitmex.Bot.model.enums.TypeData;
import bitmex.Bot.model.DatesTimes;
import bitmex.Bot.model.Gasket;
import bitmex.Bot.view.ConsoleHelper;


public class TestOrderSellPatternUser extends Thread {

    private double priseTakeOrder;
    private double priseStopOrder;
    private double priseOpenOrder;
    private String ID;

    public TestOrderSellPatternUser(String zeroString, double priseOpenOrder) {
        this.priseTakeOrder = priseOpenOrder - Gasket.getTake();
        this.priseStopOrder = priseOpenOrder + Gasket.getStop();
        this.priseOpenOrder = priseOpenOrder;
        this.ID = zeroString;
        start();
    }



    @Override
    public void run() {
        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                + ID + " --- RUN класса TestOrderSellPatternUser начал считать");

        while (true) {
            double priceAsk = Gasket.getBitmexQuote().getAskPrice();
            double priceBid = Gasket.getBitmexQuote().getBidPrice();

            if (priceAsk >= priseStopOrder) {
                flag();
                setStop();

                // меняем число положительных / отрицательных сделок
                // а так же устанавливаем знаки для предсказателя
                String data = (Integer.parseInt(StringHelper.giveData(TypeData.BUY, ID)) + 1) + "";
//                String out = StringHelper.setData(TypeData.PREDICTOR, TypeData.LOSS.toString(), ID);
                String out = StringHelper.setData(TypeData.BUY, data, ID);

//                String[] strings = ID.split("===");
//                strings[1] = (Integer.parseInt(strings[1]) + 1) + "";
//
//                StringBuilder stringBuilder = new StringBuilder();
//
//                for (int i = 0; i < strings.length; i++) {
//                    stringBuilder.append(strings[i]);
//
//                    if (i != strings.length - 1) {
//                        stringBuilder.append("===");
//                    }
//                }

//                new UpdatingStatisticsDataUser(stringBuilder.toString());
                new UpdatingStatisticsDataUser(out);

                ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                        + ID + " --- Сработал СТОП ЛОСС USER");

                Gasket.setPROFIT_Sell_PAT(Gasket.getPROFIT_Sell_PAT() - Gasket.getStop());
                break;
            }

            if (priceBid <= priseTakeOrder) {
                flag();
                setTake();

                // меняем число положительных / отрицательных сделок
                // а так же устанавливаем знаки для предсказателя
                String data = (Integer.parseInt(StringHelper.giveData(TypeData.SELL, ID)) + 1) + "";
//                String out = StringHelper.setData(TypeData.PREDICTOR, TypeData.TAKE.toString(), ID);
                String out = StringHelper.setData(TypeData.SELL, data, ID);

//                String[] strings = ID.split("===");
//                strings[3] = (Integer.parseInt(strings[3]) + 1) + "";
//
//                StringBuilder stringBuilder = new StringBuilder();
//
//                for (int i = 0; i < strings.length; i++) {
//                    stringBuilder.append(strings[i]);
//
//                    if (i != strings.length - 1) {
//                        stringBuilder.append("===");
//                    }
//                }

//                new UpdatingStatisticsDataUser(stringBuilder.toString());
                new UpdatingStatisticsDataUser(out);

                ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                        + ID + " --- Сработал ТЕЙК ПРОФИТ USER");

                Gasket.setPROFIT_Sell_PAT(Gasket.getPROFIT_Sell_PAT() + Gasket.getTake());
                break;
            }

            try {
                Thread.sleep(Gasket.getSECOND());
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                        + ID + " --- Не смогли проснуться в методе RUN класса TestOrderSellPatternUser");
            }
        }
        ConsoleHelper.printStatisticsPatterns();
    }





    private void flag() {
//        if (Gasket.getStrategyWorkOne() == 1) Gasket.setOb_os_Flag(true);
//        else if (Gasket.getStrategyWorkOne() == 2) {
//            if (!Gasket.isOsFlag_4()) Gasket.setOsFlag_4(true);
//            if (!Gasket.isOsFlag_3()) Gasket.setOsFlag_3(true);
//            if (!Gasket.isOsFlag_2()) Gasket.setOsFlag_2(true);
//            if (!Gasket.isOsFlag()) Gasket.setOsFlag(true);
//        }
    }

    private void setStop() {
        Gasket.setOsStopPat(Gasket.getOsStopPat() + 1);
    }

    private void setTake() {
        Gasket.setOsTakePat(Gasket.getOsTakePat() + 1);
    }
}

