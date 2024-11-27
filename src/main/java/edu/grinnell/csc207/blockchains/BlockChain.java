package edu.grinnell.csc207.blockchains;

import java.security.NoSuchAlgorithmException;
import edu.grinnell.csc207.util.AssociativeArray;
import edu.grinnell.csc207.util.KeyNotFoundException;
import edu.grinnell.csc207.util.NullKeyException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A full blockchain.
 *
 * @author Jafar Jarrar
 * @author Mitch Paiva
 */
public class BlockChain implements Iterable<Transaction> {
  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The first block in the blockchain.
   */
  Block firstBlock;

  /**
   * The last block in the blockchain.
   */
  Block lastBlock;

  // HashValidator simpleValidator;

  // HashValidator standardValidator =
  //   (hash) -> (hash.length() >= 3) && (hash.get(0) == 0)
  //     && (hash.get(1) == 0) && (hash.get(2) == 0);

  /**
   * The Hashvalidator that will be used to check if Hash values are valid.
   */
  HashValidator validator;

  /**
   * Stores all the usernames being used in a blockchain.
   */
  ArrayList<String> userNames;

  /**
   * Stores all the transactions made in the blockchain.
   */
  ArrayList<Transaction> allTransactions;

  /**
   * Stores the number of blocks in the blockchain, inclduing the first block
   * created in the constructor.
   */
  int numBlocks;

  /**
   * Stores the balances of each user in the blockchain in key, value pairs.
   * The key is the user's name and the value is the available balance.
   */
  AssociativeArray<String, Integer> userBalances;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new blockchain using a validator to check elements.
   *
   * @param check
   *   The validator used to check elements.
   */
  public BlockChain(HashValidator check) {
    this.firstBlock = new Block(0, new Transaction("", "", 0),
        new Hash(new byte[] {}), check);
    this.userNames = new ArrayList<String>();
    allTransactions = new ArrayList<Transaction>();
    userBalances = new AssociativeArray<String, Integer>();
    numBlocks = 1;
    firstBlock.next = null;
    this.lastBlock = firstBlock;
    this.validator = check;
  } // BlockChain(HashValidator)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Checks if a given transaction is valid based on available users and
   * user balances.
   * @param transaction the transaction to be checked.
   * @return true if the transaction is valid, false if not.
   */
  public boolean validTransaction(Transaction transaction) {
    if (transaction.getAmount() < 0) {
      return false;
    } else if (transaction.getSource().equals("")) {
      return true;
    } else if (!userNames.contains(transaction.getSource())) {
      return false;
    } else if ((balance(transaction.getSource()) + transaction.getAmount())
        < transaction.getAmount()) {
      return false;
    } // if
    return true;
  } // validTransaction(Transaction)

  /**
   * Checks if a hash contains the correct bytes according to the data in its block.
   * Creates a correct hash value for the block and checks if it is equal to the
   * block's actual Hash.
   * @param blk The block containing the hash to be checked.
   * @return true if the hash contains the correct contents, false if not.
   */
  public boolean validHashContents(Block blk) {
    try {
      Hash correctHash = blk.computeHash();
      if (!blk.getHash().equals(correctHash)) {
        return false;
      } // if
    } catch (NoSuchAlgorithmException e) {
      // Does nothing
    } // try/catch
    return true;
  } // validHashContents(Block)

  /**
   * Updates the user balances associative array based on the values in the
   * given transaction. Changes the source and target's user balances based
   * on the order of the transactions in the blockchain and the amount of
   * the transaction.
   * @param transaction the transaction that the balances will be updated
   * based upon.
   */
  public void updateUserBalances(Transaction transaction) {
    if (transaction.getSource().equals("")) {
      if (!userNames.contains(transaction.getTarget())) {
        userNames.add(transaction.getTarget());
        try {
          userBalances.set(transaction.getTarget(), transaction.getAmount());
        } catch (NullKeyException e) {
          // Does nothing.
        } // try/catch
      } else {
        try {
          int updatedBalance = userBalances.get(transaction.getTarget())
              + transaction.getAmount();
          userBalances.set(transaction.getTarget(), updatedBalance);
        } catch (Exception e) {
          // Does nothing.
        } // try/catch
      } // if
    } else {
      if (!userNames.contains(transaction.getTarget())) {
        userNames.add(transaction.getTarget());
        try {
          userBalances.set(transaction.getTarget(), transaction.getAmount());
          int updatedSourceBalance = userBalances.get(transaction.getSource())
              - transaction.getAmount();
          userBalances.set(transaction.getSource(), updatedSourceBalance);
        } catch (Exception e) {
          // Does nothing.
        } //
      } else {
        try {
          int updatedSourceBalance = userBalances.get(transaction.getSource())
              - transaction.getAmount();
          userBalances.set(transaction.getSource(), updatedSourceBalance);
          int updatedTargetBalance = userBalances.get(transaction.getTarget())
              + transaction.getAmount();
          userBalances.set(transaction.getTarget(), updatedTargetBalance);
        } catch (Exception e) {
          // Does Nothing.
        } // try/catch
      } // if
    } // if
  } // updateUserBalances(Transaction)

  /**
   * Helper method used in finding the previous block of a given block.
   * Returns the block with the given blockNum by iterating through the blockchain
   * and finding the block with the given blockNum.
   * @param blockNum the number of the block.
   * @return the block with the given blockNum.
   */
  public Block getBlock(int blockNum) {
    Iterator<Block> iterator = this.blocks();
    if (firstBlock.blockNum == blockNum) {
      return firstBlock;
    } // if
    Block block = null;
    while (iterator.hasNext()) {
      Block next = iterator.next();
      if (next.blockNum == blockNum) {
        block = next;
        break;
      } // if
    } // while
    return block;
  } // getPreviousBlock(int)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  /**
   * Mine for a new valid block for the end of the chain, returning that
   * block.
   *
   * @param t
   *   The transaction that goes in the block.
   *
   * @return a new block with correct number, hashes, and such.
   */
  public Block mine(Transaction t) {
    return new Block(this.numBlocks, t, getHash(), this.validator);
  } // mine(Transaction)

  /**
   * Get the number of blocks curently in the chain.
   *
   * @return the number of blocks in the chain, including the initial block.
   */
  public int getSize() {
    return this.numBlocks;
  } // getSize()

  /**
   * Add a block to the end of the chain.
   *
   * @param blk
   *   The block to add to the end of the chain.
   *
   * @throws IllegalArgumentException if (a) the hash is not valid, (b)
   *   the hash is not appropriate for the contents, or (c) the previous
   *   hash is incorrect.
   */
  public void append(Block blk) throws IllegalArgumentException {
    if (!validator.isValid(blk.getHash()) || !validHashContents(blk)
        || !blk.getPrevHash().equals(getHash())) {
      throw new IllegalArgumentException();
    } // if
    this.lastBlock.next = blk;
    this.lastBlock = blk;
    updateUserBalances(blk.getTransaction());
    allTransactions.add(blk.getTransaction());
    numBlocks++;
  } // append()

  /**
   * Attempt to remove the last block from the chain.
   *
   * @return false if the chain has only one block (in which case it's
   *   not removed) or true otherwise (in which case the last block
   *   is removed).
   */
  public boolean removeLast() {
    if (numBlocks == 1) {
      return false;
    } // if
    // Sets the user in the last block's balance to 0.
    try {
      if (lastBlock.getTransaction().getSource().equals("")) {
        int userBalance = userBalances.get(lastBlock.getTransaction().getTarget());
        userBalances.set(lastBlock.getTransaction().getTarget(), userBalance - userBalance);
      } else {
        int newSourceBalance = userBalances.get(lastBlock.getTransaction().getSource())
            + lastBlock.getTransaction().getAmount();
        userBalances.set(lastBlock.getTransaction().getSource(), newSourceBalance);
        int newTargetBalance = userBalances.get(lastBlock.getTransaction().getTarget())
            - lastBlock.getTransaction().getAmount();
        userBalances.set(lastBlock.getTransaction().getTarget(), newTargetBalance);
      } // if
    } catch (Exception e) {
      // Does nothing.
    } // try/catch

    Block prevBlock = getBlock(numBlocks - 2);
    lastBlock = prevBlock;
    lastBlock.next = null;
    numBlocks--;
    return true;
  } // removeLast()

  /**
   * Get the hash of the last block in the chain.
   *
   * @return the hash of the last block in the chain.
   */
  public Hash getHash() {
    return lastBlock.getHash();
  } // getHash()

  /**
   * Determine if the blockchain is correct in that (a) the balances are
   * legal/correct at every step, (b) that every block has a correct
   * previous hash field, (c) that every block has a hash that is correct
   * for its contents, and (d) that every block has a valid hash.
   *
   * @return true if the blockchain is correct and false otherwise.
   */
  public boolean isCorrect() {
    if (this.numBlocks == 1) {
      return true;
    } // if
    Iterator<Block> iterator = this.blocks();
    Block next = firstBlock;
    next = iterator.next();
    int blockNum = 0;
    boolean secondBlockCheck = true;
    while (iterator.hasNext()) {
      next = iterator.next();
      blockNum++;
      if (secondBlockCheck) {
        secondBlockCheck = false;
        if (!next.getTransaction().getSource().equals("")
            || !next.getPrevHash().equals(firstBlock.getHash())
            || !validHashContents(next) || !this.validator.isValid(next.getHash())) {
          return false;
        } // if
      } else {
        if (!validTransaction(next.getTransaction())
            || !next.getPrevHash().equals(getBlock(blockNum - 1).getHash())
            || !validHashContents(next) || !this.validator.isValid(next.getHash())) {
          return false;
        } // if
      } // if
    } // while
    return true;
  } // isCorrect()

  /**
   * Determine if the blockchain is correct in that (a) the balances are
   * legal/correct at every step, (b) that every block has a correct
   * previous hash field, (c) that every block has a hash that is correct
   * for its contents, and (d) that every block has a valid hash.
   *
   * @throws Exception
   *   If things are wrong at any block.
   */
  public void check() throws Exception {
    if (this.numBlocks == 1) {
      return;
    } // if
    Iterator<Block> iterator = this.blocks();
    Block next = firstBlock;
    next = iterator.next();
    int blockNum = 0;
    boolean secondBlockCheck = true;
    while (iterator.hasNext()) {
      next = iterator.next();
      blockNum++;
      if (secondBlockCheck) {
        secondBlockCheck = false;
        if (!next.getTransaction().getSource().equals("")) {
          throw new Exception("The first block added to the chain should not have a source name.");
        } else if (!next.getPrevHash().equals(this.firstBlock.getHash())) {
          throw new Exception(
            "The first block added to the chain has an invalid prevHash value.");
        } else if (!validHashContents(next)) {
          throw new Exception(
            "The first block added to the chain's contents do not match its hash value.");
        } else if (!this.validator.isValid(next.getHash())) {
          throw new Exception("The first block added to the chain has an invalid hash value.");
        } // if
      } else {
        if (!validTransaction(next.getTransaction())) {
          throw new Exception("Block number " + blockNum + "'s transaction is incorrect.");
        } else if (!next.getPrevHash().equals(getBlock(blockNum - 1).getHash())) {
          throw new Exception("Block number " + blockNum + "'s prevHash value does not match block"
              + (blockNum - 1) + "'s hash value.");
        } else if (!validHashContents(next)) {
          throw new Exception("Block number " + blockNum
              + "'s contents do not match its hash value.");
        } else if (!this.validator.isValid(next.getHash())) {
          throw new Exception("Block number " + blockNum + "'s hash value is invalid.");
        } // if
      } // if
    } // while
  } // check()

  /**
   * Return an iterator of all the people who participated in the
   * system.
   *
   * @return an iterator of all the people in the system.
   */
  public Iterator<String> users() {
    return new Iterator<String>() {
      int index = 0;
      public boolean hasNext() {
        return (userNames.size() >= index + 1);
      } // hasNext()

      public String next() {
        return userNames.get(index++);
      } // next()
    };
  } // users()

  /**
   * Find one user's balance.
   *
   * @param user
   *   The user whose balance we want to find.
   *
   * @return that user's balance (or 0, if the user is not in the system).
   */
  public int balance(String user) {
    int balance = 0;
    try {
      balance = userBalances.get(user);
    } catch (KeyNotFoundException e) {
      // Do nothing.
    } // try/catch
    return balance;
  } // balance()

  /**
   * Get an interator for all the blocks in the chain.
   *
   * @return an iterator for all the blocks in the chain.
   */
  public Iterator<Block> blocks() {
    return new Iterator<Block>() {
      Block current = firstBlock;
      public boolean hasNext() {
        if (current == null) {
          return false;
        } // if
        return true;
      } // hasNext()

      public Block next() {
        Block temp = current;
        current = current.next;
        return temp;
      } // next()
    };
  } // blocks()

  /**
   * Get an interator for all the transactions in the chain.
   *
   * @return an iterator for all the blocks in the chain.
   */
  public Iterator<Transaction> iterator() {
    return new Iterator<Transaction>() {
      int index = 0;
      public boolean hasNext() {
        return (allTransactions.size() >= index + 1);
      } // hasNext()

      public Transaction next() {
        return allTransactions.get(index++);
      } // next()
    };
  } // iterator()

} // class BlockChain
