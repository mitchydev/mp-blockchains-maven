package edu.grinnell.csc207.blockchains;

import java.security.NoSuchAlgorithmException;
import edu.grinnell.csc207.blockchains.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

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

  HashValidator validator;

  ArrayList<String> userNames;

  int numBlocks;

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
    this.firstBlock = new Block(0, new Transaction("", "", 0), new Hash(new byte[] {}), check);
    this.userNames = new ArrayList<String>();
    numBlocks = 1;
    firstBlock.next = null;
    this.lastBlock = firstBlock;
    userNames.add(firstBlock.getTransaction().getSource());
    this.validator = check;
  } // BlockChain(HashValidator)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  public boolean validTransaction (Transaction transaction) {
    if (transaction.getAmount() < 0) {
      return false;
    } else if (!userNames.contains(transaction.getSource())) {
      return false;
    } else if (balance(transaction.getSource()) < transaction.getAmount()) {
      return false;
    } // if
    return true;
  } // validTransaction(Transaction)

  public boolean validHashContents (Block blk) {
    try {
      Hash correctHash = blk.computeHash();
      if (!blk.getHash().equals(correctHash)) {
        return false;
      } // if
    } catch (NoSuchAlgorithmException e) {
      // Does nothing
    } // try/catch
    return true;
  }

  /**
   * Helper method used in finding the previous block of a given block.
   * Returns the block with the given blockNum by iterating through the blockchain
   * and finding the block with the given blockNum.
   * @param blockNum the number of the block.
   * @return the block with the given blockNum.
   */
  public Block getBlock (int blockNum) {
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
    return new Block(this.numBlocks++, t, getHash(), this.validator);
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
    if (!validator.isValid(blk.getHash()) || !validHashContents(blk) || !blk.getPrevHash().equals(getHash())) {
      throw new IllegalArgumentException();
    } // if
    this.lastBlock.next = blk;
    this.lastBlock = blk;
    this.userNames.add(blk.getTransaction().getSource());
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
    Iterator<Block> iterator = this.blocks();
    Block next = firstBlock;
    int blockNum = 0;
    boolean secondBlockCheck = true;
    while (iterator.hasNext()) {
      next = iterator.next();
      blockNum++;
      if (secondBlockCheck) {
        secondBlockCheck = false;
        if (!next.getTransaction().getSource().equals("") ||
          !next.getPrevHash().equals(firstBlock.getHash()) ||
          !validHashContents(next) || !this.validator.isValid(next.getHash())) {
          return false;
        } // if
      } else {
        if (!validTransaction(next.getTransaction()) ||
          !next.getPrevHash().equals(getBlock(blockNum).getHash()) ||
          !validHashContents(next) || !this.validator.isValid(next.getHash())) {
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
    Iterator<Block> iterator = this.blocks();
    Block next = firstBlock;
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
          throw new Exception("The first block added to the chain has an invalid prevHash value.");
        } else if (!validHashContents(next)) {
          throw new Exception("The first block added to the chain's contents do not match its hash value.");
        } else if (!this.validator.isValid(next.getHash())) {
          throw new Exception("The first block added to the chain has an invalid hash value.");
        } // if
      } else {
        if (!validTransaction(next.getTransaction())) {
          throw new Exception("Block number " + blockNum + "'s transaction is incorrect.");
        } else if (!next.getPrevHash().equals(getBlock(blockNum).getHash())) {
          throw new Exception("Block number " + blockNum + "'s prevHash value does not match block" +
            (blockNum - 1) + "'s hash value.");
        } else if (!validHashContents(next)) {
          throw new Exception("Block number " + blockNum + "'s contents do not match its hash value.");
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
      public boolean hasNext() {
        return false;   // STUB
      } // hasNext()

      public String next() {
        throw new NoSuchElementException();     // STUB
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
    return 0;   // STUB
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
        return current.next == null;
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
      public boolean hasNext() {
        return false;   // STUB
      } // hasNext()

      public Transaction next() {
        throw new NoSuchElementException();     // STUB
      } // next()
    };
  } // iterator()

} // class BlockChain
