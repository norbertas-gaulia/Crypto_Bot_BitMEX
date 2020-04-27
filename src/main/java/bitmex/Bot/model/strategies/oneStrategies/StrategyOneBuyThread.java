package bitmex.Bot.model.strategies.oneStrategies;

import bitmex.Bot.model.serverAndParser.InfoIndicator;
import bitmex.Bot.view.ConsoleHelper;
import bitmex.Bot.model.Gasket;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;

import static bitmex.Bot.model.Gasket.getTimeCalculationCombinationLevel;

public class StrategyOneBuyThread extends Thread {
    private InfoIndicator volume;
    private InfoIndicator bid;
    private String ID;
    private Date date;

    public StrategyOneBuyThread(String id, InfoIndicator volume, InfoIndicator bid) {
        this.date = new Date();
        this.volume = volume;
        this.bid = bid;
        this.ID = id;
        start();
    }


    @Override
    public void run() {
        // получаем цену на данный момент и сравниваем с бид и объемом если цена поднялась выше этих уровней -
        // то все отменяем, если же пошла вниз то заходим в сел.
        double max = volume.getPrice() + Gasket.getRangePriceMAX();
        double min = volume.getPrice() > bid.getPrice()
                ? bid.getPrice() - Gasket.getRangePriceMIN() : volume.getPrice() - Gasket.getRangePriceMIN();

        ConsoleHelper.writeMessage(ID + " --- RUN Strategy One Buy Thread начал работать ---- " + getDate());
        ConsoleHelper.writeMessage(ID + " --- MAX ---- " + max + " --- MIN --- " + min);

        while (true) {

            double close = Gasket.getBitmexQuote().getAskPrice();

            if (close < min || !isRelevantDate()) {
                flag();
                if (!isRelevantDate()) {
                    ConsoleHelper.writeMessage(ID + " --- Сделка Бай ОТМЕНЕНА по дате комбинации---- " + getDate());
                } else if (Gasket.isUseStopLevelOrNotStop() && close < min) {
                    ConsoleHelper.writeMessage(ID + " --- Сделка Бай ВЫШЛА ЗА уровень MIN ---- " + getDate());
                    new StopBuyTimeThread(ID, max, min);
                } else {
                    ConsoleHelper.writeMessage(ID + " --- Сделка Бай ОТМЕНЕНА ---- " + getDate());
                }
                return;
            }

            if (close > max && isRelevantDate()) {
                if (Gasket.isTrading()) new TradeBuy(ID);
                ConsoleHelper.writeMessage(ID + " --- Сделал сделку Бай ---- " + getDate());
                new TestOrderBuy(ID, close).start();
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage(ID
                        + " --- Не смогли проснуться в методе RUN класса Strategy One Sell.");
                e.printStackTrace();
            }
        }
    }

    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        dateFormat.format(date);
        date.setTime(Gasket.getDateDifference() > 0
                ? date.getTime() + (1000 * 60 * 60 * Math.abs(Gasket.getDateDifference()))
                : date.getTime() - (1000 * 60 * 60 * Math.abs(Gasket.getDateDifference())));
        return dateFormat.format(date);
    }

    private void flag() {
        if (Gasket.getStrategyWorkOne() == 1) Gasket.setOb_os_Flag(true);
        else if (Gasket.getStrategyWorkOne() == 2) {
            if (!Gasket.isObFlag_4()) Gasket.setObFlag_4(true);
            if (!Gasket.isObFlag_3()) Gasket.setObFlag_3(true);
            if (!Gasket.isObFlag_2()) Gasket.setObFlag_2(true);
            if (!Gasket.isObFlag()) Gasket.setObFlag(true);
        }
    }

    private boolean isRelevantDate() {
        Date dateNow = new Date();
        if ((dateNow.getTime() - date.getTime()) < (long) (1000 * 60 * getTimeCalculationCombinationLevel())) {
            return true;
        } else return false;
    }
}

