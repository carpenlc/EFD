/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.util;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Pluggable hash functions, based off of CPL code at:
 * http://www.partow.net/programming/hashfunctions/
 * 
 * FNV-1a, FORTH, MD5 added by gvanore
 * 
 */
public enum HashFunction {
    /**
     * An algorithm produced by Arash Partow. It takes ideas from all of the above hash functions,
     * making a hybrid rotative and additive hash function algorithm. There isn't any real
     * mathematical analysis explaining why one should use this hash function instead of the others
     */
    AP {
        public long hash(String str) {
            return APHash(str);
        }
    },
    /**
     * This hash function comes from Brian Kernighan and Dennis Ritchie's book "The C Programming
     * Language". It is a simple hash function using a strange set of possible seeds which all
     * constitute a pattern of 31....31...31 etc, it seems to be very similar to the DJB hash function.
     */
    BKDR {
        public long hash(String str) {
            return BKDRHash(str);
        }
    },
    BP {
        public long hash(String str) {
            return BPHash(str);
        }
    },
    /**
     * An algorithm proposed by Donald E. Knuth in The Art Of Computer Programming Volume 3, under
     * the topic of sorting and search chapter 6.4.
     */
    DEK {
        public long hash(String str) {
            return DEKHash(str);
        }
    },
    /**
     * An algorithm produced by Professor Daniel J. Bernstein and shown first to the world on the
     * usenet newsgroup comp.lang.c. It is one of the most efficient hash functions ever published.
     */
    DJB {
        public long hash(String str) {
            return DJBHash(str);
        }
    },
    /**
     * Similar to the PJW Hash function, but tweaked for 32-bit processors. Its the hash
     * function widely used on most UNIX systems.
     */
    ELF {
        public long hash(String str) {
            return ELFHash(str);
        }
    },
    /**
     * The famous FNV hash algorithm.
     */
    FNV {
        public long hash(String str) {
            return FNVHash(str);
        }
    },
    /**
     * The -1a variation of FNV, which reverses the order of the xor and multiply.
     * Some people use FNV-1a instead of FNV-1 because they see slightly better dispersion
     * for tiny (<4 octets) chunks of memory.
     */
    FNV1a {
        public long hash(String str) {
            return FNV1aHash(str);
        }
    },
    /**
     * A bitwise hash function written by Justin Sobel
     */
    JS {
        public long hash(String str) {
            return JSHash(str);
        }
    },
    /**
     * This hash algorithm is based on work by Peter J. Weinberger of AT&T Bell Labs.
     * The book Compilers (Principles, Techniques and Tools) by Aho, Sethi and Ulman,
     * recommends the use of hash functions that employ the hashing methodology found
     * in this particular algorithm.
     */
    PJW {
        public long hash(String str) {
            return PJWHash(str);
        }
    },
    /**
     * A simple hash function from Robert Sedgwicks Algorithms in C book, with some
     * optimizations to speed up the hashing process.
     */
    RS {
        public long hash(String str) {
            return RSHash(str);
        }
    },
    /**
     * This is the algorithm of choice which is used in the open source SDBM project.
     * The hash function seems to have a good over-all distribution for many different
     * data sets. It seems to work well in situations where there is a high variance in
     * the MSBs of the elements in a data set.
     */
    SDBM {
        public long hash(String str) {
            return SDBMHash(str);
        }
    },
    /**
     * Java's default, built-in String hashing algorithm.
     */
    JAVA {
        public long hash(String str) {
            return str.hashCode();
        }
    },
    /**
     * The hash function from the BBL FORTH compiler.
     */
    FORTH {
        public long hash(String str) {
            return FORTHHash(str);
        }
    },
    /**
     * Use MD5 as the hashing algorithm.  Probably overkill.
     */
    MD5 {
        private MessageDigest algorithm = null;
      
        public long hash(String str) {
            try {
                if (algorithm == null) algorithm = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsae) {
                return JAVA.hash(str);
            }
            
            return DigestHash(algorithm, str);
        }
    };
    
    public abstract long hash(String str);
    
    static long RSHash(String str) {
        int b = 378551;
        int a = 63689;
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = hash * a + str.charAt(i);
            a = a * b;
        }

        return hash;
    }

    static long JSHash(String str) {
        long hash = 1315423911;

        for (int i = 0; i < str.length(); i++) {
            hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
        }

        return hash;
    }

    static long PJWHash(String str) {
        long BitsInUnsignedInt = (long) (4 * 8);
        long ThreeQuarters = (long) ((BitsInUnsignedInt * 3) / 4);
        long OneEighth = (long) (BitsInUnsignedInt / 8);
        long HighBits = (long) (0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
        long hash = 0;
        long test = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash << OneEighth) + str.charAt(i);

            if ((test = hash & HighBits) != 0) {
                hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
            }
        }

        return hash;
    }

    static long ELFHash(String str) {
        long hash = 0;
        long x = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 4) + str.charAt(i);

            if ((x = hash & 0xF0000000L) != 0) {
                hash ^= (x >> 24);
            }
            hash &= ~x;
        }

        return hash;
    }

    static long BKDRHash(String str) {
        long seed = 131; // 31 131 1313 13131 131313 etc..
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = (hash * seed) + str.charAt(i);
        }

        return hash;
    }

    static long SDBMHash(String str) {
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
        }

        return hash;
    }

    static long DJBHash(String str) {
        long hash = 5381;

        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) + hash) + str.charAt(i);
        }

        return hash;
    }

    static long DEKHash(String str) {
        long hash = str.length();

        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
        }

        return hash;
    }

    static long BPHash(String str) {
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash = hash << 7 ^ str.charAt(i);
        }

        return hash;
    }

    static long FNVHash(String str) {
        long fnv_prime = 0x811C9DC5;
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash *= fnv_prime;
            hash ^= str.charAt(i);
        }

        return hash;
    }
    
    static long FNV1aHash(String str) {
        long fnv_prime = 0x811C9DC5;
        long hash = 0;

        for (int i = 0; i < str.length(); i++) {
            hash ^= str.charAt(i);
            hash *= fnv_prime;
        }

        return hash;
    }
    
    static long APHash(String str) {
        long hash = 0xAAAAAAAA;

        for (int i = 0; i < str.length(); i++) {
            if ((i & 1) == 0) {
                hash ^= ((hash << 7) ^ str.charAt(i) ^ (hash >> 3));
            } else {
                hash ^= (~((hash << 11) ^ str.charAt(i) ^ (hash >> 5)));
            }
        }

        return hash;
    }
    
    static long FORTHHash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); ++i) {
           hash <<= 1;
           if (hash < 0) {
              hash |= 1;
           }
           hash ^= str.charAt(i);
        }
        return hash;
    }
    
    static long DigestHash(MessageDigest digest, String str) {
        byte[] hash = digest.digest(str.getBytes());
        DataInput in = new DataInputStream(new ByteArrayInputStream(hash));
        long result = 0;
        try {
            while (true) {
                result |= in.readLong();
            }
        } catch (IOException ioe) {
            return result;
        }
    }    
}
