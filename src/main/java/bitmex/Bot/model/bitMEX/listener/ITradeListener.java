/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitmex.Bot.model.bitMEX.listener;

import bitmex.Bot.model.bitMEX.entity.BitmexTrade;

/**
 *
 * @author RobTerpilowski
 */
public interface ITradeListener {
    
    public void tradeUpdated(BitmexTrade trade);
}
