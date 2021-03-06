package bitmex.Bot.model.strategies.IIUser;

import bitmex.Bot.model.*;
import bitmex.Bot.model.enums.TypeData;
import bitmex.Bot.view.ConsoleHelper;

import java.util.ArrayList;
import java.util.Arrays;

import static bitmex.Bot.model.Gasket.isIndentPriceOnOff;
import static bitmex.Bot.model.StringHelper.giveData;
import static bitmex.Bot.model.enums.TypeData.*;
import static bitmex.Bot.view.ConsoleHelper.writeMessage;


public class MakeDealMartingale extends Thread {
    private OpenTransactions openTransactions;
    private ArrayList<String> marketList;
    private String patternZeroString;
    private Martingale martingale;
    private String steeps;
    private String IDs;


    public MakeDealMartingale(ArrayList<String> marketList, String patternZeroString) {
        this.steeps = StringHelper.giveData(MARTINGALE, patternZeroString);
        this.IDs = StringHelper.giveData(TypeData.ID, patternZeroString);
        this.openTransactions = Gasket.getOpenTransactions();
        this.martingale = Gasket.getMartingaleClass();
        this.marketList = new ArrayList<>(marketList);
        this.patternZeroString = patternZeroString;
        start();
    }



    @Override
    public void run() {
        writeMessage(DatesTimes.getDateTerminal() + " --- "
                + "Определяю какую сделку сделать согласно ПАТТЕРНАМ USER " + MARTINGALE.toString());

        if (patternZeroString.endsWith(TEST.toString())) {
            // проверяем есть ли такой айди в мапе
            if (openTransactions.isThereSuchKeyTest(IDs)) {
                // если есть то проверяем равен ли он нулю, что значит в данный момент все сделки закрыты
                // и можно делать сделку
                if (openTransactions.getOrderVolumeTest(IDs) == 0.0) {
                    makeDeal(false);
                } else {
                    writeMessage(DatesTimes.getDateTerminal() + " --- "
                            + "В данный момент уже есть такая сделка ПАТТЕРНА USER "
                            + IDs + " - " + MARTINGALE.toString());
                }
            } else {
                openTransactions.setMapTest(IDs, 0.0);
                makeDeal(false);
            }
        } else if (patternZeroString.endsWith(REAL.toString())) {
            // проверяем есть ли такой айди в мапе
            if (openTransactions.isThereSuchKeyReal(IDs)) {
                // если есть то проверяем равен ли он нулю, что значит в данный момент все сделки закрыты
                // и можно делать сделку
                if (openTransactions.getOrderVolumeReal(IDs) == 0.0) {
                    makeDeal(true);
                } else {
                    writeMessage(DatesTimes.getDateTerminal() + " --- "
                            + "В данный момент уже есть такая сделка ПАТТЕРНА USER "
                            + IDs + " - " + MARTINGALE.toString());
                }
            } else {
                openTransactions.setMapReal(IDs, 0.0);
                makeDeal(true);
            }
        }
    }




    private void makeDeal(boolean b) {
        int sell = Integer.parseInt(giveData(SELL, patternZeroString));
        int buy = Integer.parseInt(giveData(BUY, patternZeroString));
        String stringOut = patternZeroString;


        if (buy > sell) {
            if (conditionsAreMet(true)) {


                // REAL
                if (b && Gasket.isTradingMartingale() && openTransactions.getOrderVolumeReal(IDs) == 0.0) {
                    if (martingale.isThereSuchKeyReal(IDs)) {
                        martingale.upSteepReal(IDs);
                    } else {
                        martingale.setMapSteepReal(IDs, 1);
                    }

                    Double lotSize = martingale.getLotForThisSteep(IDs, martingale.getSteepReal(IDs));

                    if (lotSize != null) {
                        double index = (double) Math.abs(Integer.parseInt(giveData(BUY, patternZeroString)))
                                / Math.abs(Integer.parseInt(giveData(SELL, patternZeroString)));

                        if (index >= Gasket.getIndexRatioTransactionsAtWhichEnterMarket()) {
                            openTransactions.setMapReal(IDs, lotSize);
//                            martingale.upSteepReal(IDs);

                            double lot = martingale.getLotForThisSteep(IDs, martingale.getSteepReal(IDs));
                            ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " ----- LOT === " + lot
                                    + " === STEEP === " + martingale.getSteepReal(IDs)
                            );

                            if (lot > 0) {
                                if (Gasket.getMartingaleClass().getDeal() == 0) {
                                    new TradeBuyPro(stringOut, lot);
                                    Gasket.getMartingaleClass().setDeal(1);
                                }
                                new TestOrderBuyPatternMartingale(true, stringOut, Gasket.getBitmexQuote().getAskPrice());
                            }
                        }

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделал сделку БАЙ USER " + MARTINGALE.toString() + " - REAL"
                        );
                    } else {
                        martingale.downSteepReal(IDs);

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделку БАЙ USER " + MARTINGALE.toString()
                                + " ОТМЕНИЛ --- перевышен МАКСИМАЛЬНЫЙ шаг - REAL"
                        );
                    }
                }



                // TEST
                if (!b && Gasket.isTradingTestMartingale() && openTransactions.getOrderVolumeTest(IDs) == 0.0) {
                    if (martingale.isThereSuchKey(IDs)) {
                        martingale.upSteep(IDs);
                    } else {
                        martingale.setMapSteep(IDs, 1);
                    }

                    Double lotSize = martingale.getLotForThisSteep(IDs, martingale.getSteep(IDs));


                    if (lotSize != null) {
                        openTransactions.setMapTest(IDs, lotSize);

                        stringOut = Integer.parseInt(steeps) > martingale.getSteep(IDs) ? stringOut
                                : StringHelper.setData(MARTINGALE, martingale.getSteep(IDs) + "", stringOut);

                        new TestOrderBuyPatternMartingale(stringOut, Gasket.getBitmexQuote().getAskPrice());

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделал сделку БАЙ USER - TEST " + MARTINGALE.toString() + " - по цене - "
                                + Gasket.getBitmexQuote().getAskPrice()
                        );
                    } else {
                        martingale.downSteep(IDs);

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделку БАЙ USER " + MARTINGALE.toString()
                                + " ОТМЕНИЛ --- перевышен МАКСИМАЛЬНЫЙ шаг - TEST"
                        );
                    }
                }
            } else {
                ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                        + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                        + " сделку БАЙ USER " + MARTINGALE.toString() + " ОТМЕНИЛ по истечению ВРЕМЕНИ"
                );
            }

        } else if (buy < sell) {
            if (conditionsAreMet(false)) {


                // REAL
                if (b && Gasket.isTradingMartingale() && openTransactions.getOrderVolumeReal(IDs) == 0.0) {

                    if (martingale.isThereSuchKeyReal(IDs)) {
                        martingale.upSteepReal(IDs);
                    } else {
                        martingale.setMapSteepReal(IDs, 1);
                    }


                    Double lotSize = martingale.getLotForThisSteep(IDs, martingale.getSteepReal(IDs));

                    if (lotSize != null) {
                        double index = (double) Math.abs(Integer.parseInt(giveData(SELL, patternZeroString)))
                                / Math.abs(Integer.parseInt(giveData(BUY, patternZeroString)));

                        if (index >= Gasket.getIndexRatioTransactionsAtWhichEnterMarket()) {
                            openTransactions.setMapReal(IDs, lotSize);
//                            martingale.upSteepReal(IDs);

                            double lot = martingale.getLotForThisSteep(IDs, martingale.getSteepReal(IDs));

                            new TradeSellPro(stringOut, lot);
                            new TestOrderSellPatternMartingale(true, stringOut, Gasket.getBitmexQuote().getAskPrice());
                        }

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделал сделку СЕЛЛ USER " + MARTINGALE.toString() + " - REAL"
                        );
                    } else {
                        martingale.downSteepReal(IDs);
                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделку СЕЛЛ USER " + MARTINGALE.toString()
                                + " ОТМЕНИЛ --- перевышен МАКСИМАЛЬНЫЙ шаг - REAL"
                        );
                    }
                }



                // TEST
                if (!b && Gasket.isTradingTestMartingale() && openTransactions.getOrderVolumeTest(IDs) == 0.0) {

                    if (martingale.isThereSuchKey(IDs)) {
                        martingale.upSteep(IDs);
                    } else {
                        martingale.setMapSteep(IDs, 1);
                    }

                    Double lotSize = martingale.getLotForThisSteep(IDs, martingale.getSteep(IDs));

                    if (lotSize != null) {
                        openTransactions.setMapTest(IDs, lotSize);

                        stringOut = Integer.parseInt(steeps) > martingale.getSteep(IDs) ? stringOut
                                : StringHelper.setData(MARTINGALE, martingale.getSteep(IDs) + "", stringOut);

                        new TestOrderSellPatternMartingale(stringOut, Gasket.getBitmexQuote().getBidPrice());

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделал сделку СЕЛЛ USER - TEST " + MARTINGALE.toString() + " - по цене - "
                                + Gasket.getBitmexQuote().getBidPrice()
                        );
                    } else {
                        martingale.downSteep(IDs);

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                                + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                                + " сделку БАЙ USER " + MARTINGALE.toString()
                                + " ОТМЕНИЛ --- перевышен МАКСИМАЛЬНЫЙ шаг - TEST"
                        );
                    }
                }

            } else {
                ConsoleHelper.writeMessage(DatesTimes.getDateTerminal() + " --- "
                        + stringOut + " --- Согластно ПАТТЕРНУ " + giveData(TypeData.ID, patternZeroString)
                        + " сделку СЕЛЛ USER " + MARTINGALE.toString() + " ОТМЕНИЛ по истечению ВРЕМЕНИ"
                );
            }
        }
    }




    // BUY===1===SELL===0===AVERAGE===3.28===MAX===5.0===SIZE===220===BLOCK===1===TYPE===ASK===ID===4
    // тут мы находим цену выше которой надо подняться или опустится в течении определенного времени
    // что бы сделать сделку иначе отбой
    private boolean conditionsAreMet(boolean b) {
        long timeStop = 60 * Gasket.getTimeStopLiveForUserPatterns();
        int blockSearch = 1;
        double prices = 0.0;
        String types = "";
        int blocks = 0;
        long time = 0;

        if (!giveData(TypeData.BLOCK, patternZeroString).equalsIgnoreCase(NULL.toString())) {

            blocks = Integer.parseInt(giveData(TypeData.BLOCK, patternZeroString));
            types = giveData(TypeData.TYPE, patternZeroString);

            if (types.equalsIgnoreCase(NULL.toString())) {
                if (b) {
                    // если сделка бай и тайп null то сразу берем цену первой строки нужного блока
                    for (String string : marketList) {
                        if (string.startsWith(BIAS.toString())) {
                            blockSearch++;
                        }

                        if (blocks == blockSearch) {
                            prices = Double.parseDouble(giveData(TypeData.price,
                                    marketList.get(marketList.indexOf(string) + 1)));
                            break;
                        }

                    }
                } else {
                    // если сделка селл и тайп null то сразу берем цену последней строки нужного блока
                    for (String string : marketList) {
                        if (string.startsWith(BIAS.toString())) {
                            blockSearch++;
                        }

                        if (blocks + 1 == blockSearch) {
                            prices = Double.parseDouble(giveData(TypeData.price,
                                    marketList.get(marketList.indexOf(string) - 1)));
                            break;
                        }
                    }
                }
            } else {
                for (String string : marketList) {
                    if (!string.startsWith(BIAS.toString()) && !string.startsWith(BUY.toString())
                            && !string.startsWith(NULL.toString())) {

                        if (blocks == blockSearch) {
                            if (giveData(TypeData.type, string).equalsIgnoreCase(types)) {
                                prices = Double.parseDouble(giveData(TypeData.price, string));
                            }
                        }
                    } else if (string.startsWith(BIAS.toString())) {
                        blockSearch++;
                    }
                }
            }
        } else {
            prices = Gasket.getBitmexQuote().getAskPrice();
        }

        // тут мы по разному отслеживаем точку входа в рынок
        // в первом случаи сразу по условного расчетного уровня пробою входим в рынок
        // во втором после пробоя дожидаемся отката и в случаи его только потом входим в рынок
        if (!isIndentPriceOnOff()) {

            while (time < timeStop) {

                if (b) {
                    if (Gasket.getBitmexQuote().getBidPrice() > prices) {
                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal()
                                + " --- цена уровня - " + types + " - " + prices
                                + " - пробита - " + MARTINGALE.toString());
                        return true;
                    }
                } else {
                    if (Gasket.getBitmexQuote().getAskPrice() < prices) {
                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal()
                                + " --- цена уровня - " + types + " - " + prices
                                + " - пробита - " + MARTINGALE.toString());
                        return true;
                    }
                }

                try {
                    Thread.sleep(Gasket.getSECOND());
                } catch (InterruptedException e) {
                    ConsoleHelper.writeERROR(Arrays.toString(e.getStackTrace()));
                }

                time++;
            }
        } else {
            double indentPriceOut = 0.0;
            // расчитывает виртуальный тейк, чтобы отслеживать его и не нарваться на разворот
            // если цена сходит к нему раньше чем к основному ордеру - отменяем сделку
            double virtTakeOut = b ? Gasket.getBitmexQuote().getAskPrice() + Gasket.getTake()
                    : Gasket.getBitmexQuote().getBidPrice() - Gasket.getTake();


            while (time < timeStop) {

                if (b) {
                    if (Gasket.getBitmexQuote().getBidPrice() > prices) {
                        indentPriceOut = Gasket.getBitmexQuote().getAskPrice() - Gasket.getIndentPrice();

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal()
                                + " --- цена уровня - " + types + " - " + prices
                                + " - пробита - " + MARTINGALE.toString() + " жду пробития цены отката - "
                                + indentPriceOut
                        );
                        break;
                    }
                } else {
                    if (Gasket.getBitmexQuote().getAskPrice() < prices) {
                        indentPriceOut = Gasket.getBitmexQuote().getBidPrice() + Gasket.getIndentPrice();

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal()
                                + " --- цена уровня - " + types + " - " + prices
                                + " - пробита - " + MARTINGALE.toString() + " жду пробития цены отката - "
                                + indentPriceOut
                        );
                        break;
                    }
                }

                try {
                    Thread.sleep(Gasket.getSECOND());
                } catch (InterruptedException e) {
                    ConsoleHelper.writeERROR(Arrays.toString(e.getStackTrace()));
                }

                time++;
            }

//            while (time < timeStop) {
            while (true) {

                if (b) {
                    if (virtTakeOut <= Gasket.getBitmexQuote().getBidPrice()) {
                        return false;
                    }

                    if (indentPriceOut != 0.0 && Gasket.getBitmexQuote().getAskPrice() < indentPriceOut) {

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal()
                                + " --- цена уровня отката - " + types + " - " + indentPriceOut
                                + " - пробита - " + MARTINGALE.toString()
                        );
                        return true;
                    }
                } else {
                    if (virtTakeOut >= Gasket.getBitmexQuote().getAskPrice()) {
                        return false;
                    }

                    if (indentPriceOut != 0.0 && Gasket.getBitmexQuote().getBidPrice() > indentPriceOut) {

                        ConsoleHelper.writeMessage(DatesTimes.getDateTerminal()
                                + " --- цена уровня - " + types + " - " + indentPriceOut
                                + " - пробита - " + MARTINGALE.toString()
                        );
                        return true;
                    }
                }

                try {
                    Thread.sleep(Gasket.getSECOND());
                } catch (InterruptedException e) {
                    ConsoleHelper.writeERROR(Arrays.toString(e.getStackTrace()));
                }

                time++;
            }
        }
        return false;
    }
}

