package com.cbibhor.eme.expensemanagereconomizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bibhor Chauhan on 19-04-2017.
 */

public class HomeMenuData {
    public static String[] placeNameArray = {"Offers","Set Limit","All Expenses","Add New"};

    public static List<HomeMenu> setHomeMenuList() {
        List<HomeMenu> homeMenuList = new ArrayList<>();
        for (int i = 0; i < placeNameArray.length; i++) {
            HomeMenu menuItem = new HomeMenu(placeNameArray[i]);
            homeMenuList.add(menuItem);
        }
        return (homeMenuList);
    }
}
