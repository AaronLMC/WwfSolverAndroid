package com.almc.wwfsolver;

import java.util.HashMap;


    public class GameVals
    {
        public final static int BOARD_SIZE = 15;
        public final static int AVAILABLE_LETTER_MAX = 7;
        public final static int BOARD_CENTER_LOC = 7;
        public final static int BONUS_USED_ALL_TILES = 35;
        public final static char BLANK_TILE = '_';
        public static final char[] ALPHABET = new char[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

        public static final HashMap<Character, Integer> LETTER_SCORE = new HashMap<Character, Integer>();        
        static
        {
        	LETTER_SCORE.put('A', 1);
            LETTER_SCORE.put('B', 4);
            LETTER_SCORE.put('C', 4);
            LETTER_SCORE.put('D', 2);
            LETTER_SCORE.put('E', 1);
            LETTER_SCORE.put('F', 4);
            LETTER_SCORE.put('G', 3);
            LETTER_SCORE.put('H', 3);
            LETTER_SCORE.put('I', 1);
            LETTER_SCORE.put('J', 10);
            LETTER_SCORE.put('K', 5);
            LETTER_SCORE.put('L', 2);
            LETTER_SCORE.put('M', 4);
            LETTER_SCORE.put('N', 2);
            LETTER_SCORE.put('O', 1);
            LETTER_SCORE.put('P', 4);
            LETTER_SCORE.put('Q', 10);
            LETTER_SCORE.put('R', 1);
            LETTER_SCORE.put('S', 1);
            LETTER_SCORE.put('T', 1);
            LETTER_SCORE.put('U', 2);
            LETTER_SCORE.put('V', 5);
            LETTER_SCORE.put('W', 4);
            LETTER_SCORE.put('X', 8);
            LETTER_SCORE.put('Y', 3);
            LETTER_SCORE.put('Z', 10);
        };

        enum Bonus
        {
            NONE,
            D_LT,
            T_LT,
            D_WD,
            T_WD
        }

        public static final Bonus[][] BONUS_TILES = new Bonus[][]
        {
            {Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_WD, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.T_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE},
            {Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE},
            {Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE},
            {Bonus.T_WD, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.T_WD},
            {Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE},
            {Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE},
            {Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT},
            {Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE},
            {Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT},
            {Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE},
            {Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE},
            {Bonus.T_WD, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.T_WD},
            {Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE},
            {Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.D_WD, Bonus.NONE, Bonus.NONE, Bonus.D_LT, Bonus.NONE, Bonus.NONE},
            {Bonus.NONE, Bonus.NONE, Bonus.NONE, Bonus.T_WD, Bonus.NONE, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.T_LT, Bonus.NONE, Bonus.NONE, Bonus.T_WD, Bonus.NONE, Bonus.NONE, Bonus.NONE},
        };
    }

