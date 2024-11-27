package edu.grinnell.csc207.util;

import static java.lang.reflect.Array.newInstance;

/**
 * A basic implementation of Associative Arrays with keys of type K
 * and values of type V. Associative Arrays store key/value pairs
 * and permit you to look up values by key.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @author Your Name Here
 * @author Jafar Jarrar
 */
public class AssociativeArray<K, V> {
  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The default capacity of the initial array.
   */
  static final int DEFAULT_CAPACITY = 16;

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The size of the associative array (the number of key/value pairs).
   */
  int size;

  /**
   * The array of key/value pairs.
   */
  KVPair<K, V>[] pairs;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new, empty associative array.
   */
  @SuppressWarnings({ "unchecked" })
  public AssociativeArray() {
    // Creating new arrays is sometimes a PITN.
    this.pairs = (KVPair<K, V>[]) newInstance((new KVPair<K, V>()).getClass(),
        DEFAULT_CAPACITY);
    this.size = 0;
  } // AssociativeArray()

  // +------------------+--------------------------------------------
  // | Standard Methods |
  // +------------------+

  /**
   * Create a copy of this AssociativeArray.
   *
   * @return a new copy of the array
   */
  public AssociativeArray<K, V> clone() {
    AssociativeArray<K, V> newAA = new AssociativeArray<K, V>();
    if (this.size() == 0) {
      return newAA;
    } // if
    try {
      for (KVPair<K, V> pair : this.pairs) {
        newAA.set(pair.key, pair.val);
      } // for
      newAA.size = this.size();
    } catch (Exception e) {
      // Does nothing.
    } // try/catch
    return newAA;
  } // clone()

  /**
   * Convert the array to a string.
   *
   * @return a string of the form "{Key0:Value0, Key1:Value1, ... KeyN:ValueN}"
   */
  public String toString() {
    String result = "{";
    for (int i = 0; i < this.pairs.length; i++) {
      if (this.pairs[i] == null) {
        break;
      } else if (this.pairs[i + 1] == null) {
        result += this.pairs[i].key + ":" + this.pairs[i].val;
      } else {
        result += this.pairs[i].key + ":" + this.pairs[i].val + ", ";
      } // if
    } // for
    result += "}";
    return result;
  } // toString()

  // +----------------+----------------------------------------------
  // | Public Methods |
  // +----------------+

  /**
   * Set the value associated with key to value. Future calls to
   * get(key) will return value.
   *
   * @param key
   *   The key whose value we are seeting.
   * @param value
   *   The value of that key.
   *
   * @throws NullKeyException
   *   If the client provides a null key.
   */
  @SuppressWarnings({ "unchecked" })
  public void set(K key, V value) throws NullKeyException {
    if (key == null) {
      throw new NullKeyException();
    } else if (size == this.pairs.length) {
      this.expand();
      this.pairs[size] = new KVPair<K, V>(key, value);
      this.size++;
      return;
    } // if
    try {
      int index = find(key);
      this.pairs[index] = new KVPair<K, V>(key, value);
    } catch (KeyNotFoundException e) {
      this.pairs[this.size] = new KVPair<K, V>(key, value);
      this.size++;
    } // try/catch
  } // set(K,V)

  /**
   * Get the value associated with key.
   *
   * @param key
   *   A key
   *
   * @return
   *   The corresponding value
   *
   * @throws KeyNotFoundException
   *   when the key is null or does not appear in the associative array.
   */
  public V get(K key) throws KeyNotFoundException {
    try {
      int index = find(key);
      return this.pairs[index].val;
    } catch (KeyNotFoundException e) {
      throw new KeyNotFoundException("Key not found.");
    } // try/catch
  } // get(K)

  /**
   * Determine if key appears in the associative array. Should
   * return false for the null key, since it cannot appear.
   *
   * @param key
   *   The key we're looking for.
   *
   * @return true if the key appears and false otherwise.
   */
  public boolean hasKey(K key) {
    try {
      return (find(key) > -1);
    } catch (KeyNotFoundException e) {
      return false;
    } // try/catch
  } // hasKey(K)

  /**
   * Remove the key/value pair associated with a key. Future calls
   * to get(key) will throw an exception. If the key does not appear
   * in the associative array, does nothing.
   *
   * @param key
   *   The key to remove.
   */
  public void remove(K key) {
    try {
      int index = find(key);
      if (pairs[this.size - 1] == null) {
        this.pairs[index] = new KVPair<>();
      } else {
        this.pairs[index] = new KVPair<K, V>(pairs[this.size - 1].key, pairs[this.size - 1].val);
        this.pairs[size - 1] = new KVPair<>();
      } // if
      size--;
    } catch (Exception e) {
      // Does not do anything.
    } // try/catch
  } // remove(K)

  /**
   * Determine how many key/value pairs are in the associative array.
   *
   * @return The number of key/value pairs in the array.
   */
  public int size() {
    return this.size;
  } // size()

  // +-----------------+---------------------------------------------
  // | Private Methods |
  // +-----------------+

  /**
   * Expand the underlying array.
   */
  void expand() {
    this.pairs = java.util.Arrays.copyOf(this.pairs, this.pairs.length * 2);
  } // expand()

  /**
   * Find the index of the first entry in `pairs` that contains key.
   * If no such entry is found, throws an exception.
   *
   * @param keyToFind
   *   The key of the entry.
   *
   * @return
   *   The index of the key, if found.
   *
   * @throws KeyNotFoundException
   *   If the key does not appear in the associative array.
   */
  int find(K keyToFind) throws KeyNotFoundException {
    int index = 0;
    boolean found = false;
    for (int i = 0; i < this.pairs.length; i++) {
      if (this.pairs[i] == null || this.pairs[i].key == null) {
        throw new KeyNotFoundException();
      } else if (this.pairs[i].key.equals(keyToFind)) {
        index = i;
        found = true;
        break;
      } // if
    } // for
    if (found) {
      return index;
    } else {
      throw new KeyNotFoundException();
    } // else
  } // find(K)
} // class AssociativeArray
