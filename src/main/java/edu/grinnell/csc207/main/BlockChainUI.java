package edu.grinnell.csc207.main;

import edu.grinnell.csc207.blockchains.Block;
import edu.grinnell.csc207.blockchains.BlockChain;
import edu.grinnell.csc207.blockchains.HashValidator;
import edu.grinnell.csc207.blockchains.Transaction;
import edu.grinnell.csc207.blockchains.Hash;

import edu.grinnell.csc207.util.IOUtils;
import java.util.Iterator;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A simple UI for our BlockChain class.
 *
 * @author Mitch Paiva
 * @author Jafar Jarrar
 * @author Samuel A. Rebelsky
 */
public class BlockChainUI {
  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The number of bytes we validate. Should be set to 3 before submitting.
   */
  static final int VALIDATOR_BYTES = 3;

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Print out the instructions.
   *
   * @param pen
   *   The pen used for printing instructions.
   */
  public static void instructions(PrintWriter pen) {
    pen.println("""
      Valid commands:
        mine: discovers the nonce for a given transaction
        append: appends a new block onto the end of the chain
        remove: removes the last block from the end of the chain
        check: checks that the block chain is valid
        users: prints a list of users
        balance: finds a user's balance
        transactions: prints out the chain of transactions
        blocks: prints out the chain of blocks (for debugging only)
        help: prints this list of commands
        quit: quits the program""");
  } // instructions(PrintWriter)

  // +------+--------------------------------------------------------
  // | Main |
  // +------+

  /**
   * Run the UI.
   *
   * @param args
   *   Command-line arguments (currently ignored).
   */
  public static void main(String[] args) throws Exception {
    PrintWriter pen = new PrintWriter(System.out, true);
    BufferedReader eyes = new BufferedReader(new InputStreamReader(System.in));

    // Set up our blockchain.
    HashValidator validator =
        (h) -> {
          if (h.length() < VALIDATOR_BYTES) {
            return false;
          } // if
          for (int v = 0; v < VALIDATOR_BYTES; v++) {
            if (h.get(v) != 0) {
              return false;
            } // if
          } // for
          return true;
        };
    BlockChain chain = new BlockChain(validator);

    instructions(pen);

    boolean done = false;

    String source;
    String target;
    int amount;

    while (!done) {
      pen.print("\nCommand: ");
      pen.flush();
      String command = eyes.readLine();
      if (command == null) {
        command = "quit";
      } // if

      switch (command.toLowerCase()) {
        case "append":
          source = IOUtils.readLine(pen, eyes, "Source (return for deposit): ");
          target = IOUtils.readLine(pen, eyes, "Target: ");
          String amountInput = IOUtils.readLine(pen, eyes, "Amount: ");
          long nonce = IOUtils.readLong(pen, eyes, "Nonce: ");
          try {
            int amounts = Integer.parseInt(amountInput);
            Transaction transaction = new Transaction(source, target, amounts);
            int bNum = chain.getSize();
            Hash prevHash = chain.getHash();

            Block block = new Block(bNum, transaction, prevHash, nonce);

            chain.append(block);
            pen.println("Appended: " + block.toString());
          } catch (NumberFormatException exception) {
            pen.println("Invalid amount!");
          } catch (Exception exception) {
            pen.println("Could not append: Invalid hash in appended block.");
          } // try/catch
          break;

        case "balance":
          String user = IOUtils.readLine(pen, eyes, "User: ");
          pen.printf("%s's balance is %d\n", user, chain.balance(user));
          break;

        case "blocks":
          pen.println("Blocks:");
          Iterator<Block> blockIterator = chain.blocks();
          int i = 0;
          while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            pen.println(block.toString());
            i++;
          } // while
          break;

        case "check":
          try {
            chain.check();
            pen.println("The blockchain checks out.");
          } catch (Exception exception) {
            pen.println("The blockchain is invalid.");
          } // try/catch
          break;

        case "help":
          instructions(pen);
          break;

        case "mine":
          source = IOUtils.readLine(pen, eyes, "Source (return for deposit): ");
          target = IOUtils.readLine(pen, eyes, "Target: ");
          amount = IOUtils.readInt(pen, eyes, "Amount: ");
          Block b = chain.mine(new Transaction(source, target, amount));
          pen.println();
          pen.println("Use nonce: " + b.getNonce());
          break;

        case "quit":
          done = true;
          break;

        case "remove":
          if (chain.removeLast()) {
            pen.println("The last block was removed.");
          } else {
            pen.println("Unable to remove the last block.");
          } // if
          break;

        case "transactions":
          pen.println("Transactions:");
          Iterator<Transaction> transactions = chain.iterator();
          while (transactions.hasNext()) {
            pen.println(transactions.next());
          } // while
          break;

        case "users":
          Iterator<String> users = chain.users();
          while (users.hasNext()) {
            pen.println(users.next());
          } // while
          break;

        default:
          pen.printf("invalid command: '%s'. Try again.\n", command);
          break;
      } // switch
    } // while

    pen.printf("\nGoodbye\n");
    eyes.close();
    pen.close();
  } // main(String[])
} // class BlockChainUI
