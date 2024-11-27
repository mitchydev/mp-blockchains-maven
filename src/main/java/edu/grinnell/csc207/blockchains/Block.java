package edu.grinnell.csc207.blockchains;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Blocks to be stored in blockchains.
 *
 * @author Jafar Jarrar
 * @author Samuel A. Rebelsky
 */
public class Block {
  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The transaction.
   */
  Transaction transaction;

  /**
   * The nonce.
   */
  long nonce;

  /**
   * The hash of the previous block.
   */
  Hash prevBlockHash;

  /**
   * The hash of the block.
   */
  Hash blockHash;

  /**
   * The number of the block in the blockchain.
   */
  int blockNum;

  /**
   * Tells whether the hash of the block is valid.
   */
  HashValidator validator;

  /**
   * Pointer to the next block in the blockchain.
   */
  Block next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new block from the specified block number, transaction, and
   * previous hash, mining to choose a nonce that meets the requirements
   * of the validator.
   *
   * @param num
   *   The number of the block.
   * @param transaction
   *   The transaction for the block.
   * @param prevHash
   *   The hash of the previous block.
   * @param check
   *   The validator used to check the block.
   */
  public Block(int num, Transaction transaction, Hash prevHash,
      HashValidator check) {
    this.blockNum = num;
    this.transaction = transaction;
    this.prevBlockHash = prevHash;
    this.validator = check;
    mine();
  } // Block(int, Transaction, Hash, HashValidator)

  /**
   * Create a new block, computing the hash for the block.
   *
   * @param num
   *   The number of the block.
   * @param transaction
   *   The transaction for the block.
   * @param prevHash
   *   The hash of the previous block.
   * @param nonce
   *   The nonce of the block.
   */
  public Block(int num, Transaction transaction, Hash prevHash, long nonce) {
    this.blockNum = num;
    this.transaction = transaction;
    this.prevBlockHash = prevHash;
    this.nonce = nonce;
    try {
      this.blockHash = computeHash();
    } catch (NoSuchAlgorithmException e) {
      // Does Nothing
    } // try/catch
  } // Block(int, Transaction, Hash, long)

  /**
   * Looks for a valid nonce given the hashValidator check. Sets the block nonce
   * and the block hash to the values that pass the check.
   */
  public void mine() {
    for (long tempNonce = 0; tempNonce < Long.MAX_VALUE; tempNonce++) {
      try {
        this.nonce = tempNonce;
        Hash temp = computeHash();
        if (validator.isValid(temp)) {
          this.blockHash = temp;
          break;
        } // if
        this.nonce = 0;
      } catch (NoSuchAlgorithmException e) {
        // Does Nothing
      } // try/catch
    } // for
  } // mine()

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Compute the hash of the block given all the other info already
   * stored in the block.
   * @return the computed hash.
   */
  Hash computeHash() throws NoSuchAlgorithmException {
    byte[] blockNumBytes = ByteBuffer.allocate(Integer.BYTES).putInt(this.blockNum).array();
    byte[] amountBytes = ByteBuffer.allocate(Integer.BYTES).putInt(
        this.transaction.getAmount()).array();
    MessageDigest md = MessageDigest.getInstance("sha-256");
    md.update(blockNumBytes);
    md.update(this.transaction.getSource().getBytes());
    md.update(this.transaction.getTarget().getBytes());
    md.update(amountBytes);
    if (prevBlockHash != null) {
      md.update(this.prevBlockHash.getBytes());
    } // if
    byte[] nonceBytes = ByteBuffer.allocate(Long.BYTES).putLong(this.getNonce()).array();
    md.update(nonceBytes);
    return new Hash(md.digest());
  } // computeHash()

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  /**
   * Get the number of the block.
   *
   * @return the number of the block.
   */
  public int getNum() {
    return this.blockNum;
  } // getNum()

  /**
   * Get the transaction stored in this block.
   *
   * @return the transaction.
   */
  public Transaction getTransaction() {
    return this.transaction;
  } // getTransaction()

  /**
   * Get the nonce of this block.
   *
   * @return the nonce.
   */
  public long getNonce() {
    return this.nonce;
  } // getNonce()

  /**
   * Get the hash of the previous block.
   *
   * @return the hash of the previous block.
   */
  Hash getPrevHash() {
    return this.prevBlockHash;
  } // getPrevHash

  /**
   * Get the hash of the current block.
   *
   * @return the hash of the current block.
   */
  Hash getHash() {
    return this.blockHash;
  } // getHash

  /**
   * Get a string representation of the block.
   *
   * @return a string representation of the block.
   */
  public String toString() {
    if (this.transaction.getSource().equals("")) {
      return ("Block " + this.blockNum + " (Transaction: [Deposit, Target "
        + this.transaction.getTarget() + ", Amount: " + this.transaction.getAmount()
        + "], Nonce: " + this.getNonce() + ", prevHash: " + this.getPrevHash().toString()
        + ", hash: " + this.getHash().toString() + ")");
    } else {
      return ("Block " + this.blockNum + " (Transaction: [Source: " + this.transaction.getSource()
        + ", Target " + this.transaction.getTarget() + ", Amount: " + this.transaction.getAmount()
        + "], Nonce: " + this.getNonce() + ", prevHash: " + this.getPrevHash().toString()
        + ", hash: " + this.getHash().toString() + ")");
    } // if
  } // toString()
} // class Block
