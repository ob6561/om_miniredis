package com.om.miniredis;

import com.om.miniredis.commands.Command;
import com.om.miniredis.commands.CommandParser;
import com.om.miniredis.store.DataStore;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DataStore store = new DataStore();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Mini-Redis CLI. Commands: SET key value | GET key | DEL key | EXIT");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("EXIT")) {
                System.out.println("Bye.");
                return;
            }

            try {
                Command command = CommandParser.parse(line);
                System.out.println(command.execute(store));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}