package com.almc.wwfsolver;


    public class Board 
    {
        private char[][] mBoardLetters;
        private char[] mAvailableLetters;

//        public static Board Load(String filename)
//        {
//            Stream stream = File.OpenRead(filename);
//            BinaryFormatter deserializer = new BinaryFormatter();
//            Board b = (Board)deserializer.Deserialize(stream);
//            stream.Close();
//
//            return b;
//        }

        public Board(char[][] boardLetters, char[] availableLetters)
        {
            mBoardLetters = boardLetters;
            mAvailableLetters = availableLetters;
        }

//        public void SaveToFile(String filename)
//        {
//            Stream stream = File.Create(filename);
//            BinaryFormatter serializer = new BinaryFormatter();
//            serializer.Serialize(stream, this);
//            stream.Close();
//        }

        public char[][] getBoardLetters()
        {
            return mBoardLetters;
        }

        public char[] getAvailableLetters()
        {
            return mAvailableLetters;
        }
    }

