/**
 * Copyright (c) 2011 Yahoo! Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. See accompanying LICENSE file.
 */


package com.yahoo.omid.tso;

import java.util.Arrays;

public class Committed {
   
   private static final int BKT_NUMBER = 1<<15;

   private CommitBucket buckets[] = new CommitBucket[BKT_NUMBER];
   private int firstCommitedBucket = 0;
   private int lastOpenedBucket = 0;

   public Committed() {
   }

   public synchronized void commit(long id, long timestamp) {
      int position = getPosition(id);
      CommitBucket bucket = buckets[position];
      if (bucket == null) {
         bucket = new CommitBucket();
         buckets[position] = bucket;
         lastOpenedBucket = position;
      }
      bucket.commit(id, timestamp);
   }
   
   public long getCommit(long id) {
      CommitBucket bucket = buckets[getPosition(id)];
      if (bucket == null) {
         return -1;
      }
      return bucket.getCommit(id);
   }
   
   public void raiseLargestDeletedTransaction(long id) {
      int maxBucket = getPosition(id);
      for (; firstCommitedBucket != maxBucket && firstCommitedBucket != lastOpenedBucket;
            firstCommitedBucket = (firstCommitedBucket + 1) % BKT_NUMBER) {
         buckets[firstCommitedBucket] = null;
      }
   }
   
   private int getPosition(long id) {
      return ((int) (id / CommitBucket.getBucketSize())) % BKT_NUMBER;
   }
   
   public long getSize() {
       return BKT_NUMBER * 8 + (lastOpenedBucket - firstCommitedBucket) * CommitBucket.BUCKET_SIZE * 8;
   }
}

class CommitBucket {
     
   static final int BUCKET_SIZE = 1<<14;

   private long transactions [] = new long [BUCKET_SIZE];

   public CommitBucket() {
      Arrays.fill(transactions, -1);
   }

   public long getCommit(long id) {
      return transactions[(int) id % BUCKET_SIZE];
   }

   public void commit(long id, long timestamp) {
      transactions[(int) id % BUCKET_SIZE] = timestamp;
   }

   public static int getBucketSize() {
      return BUCKET_SIZE;
   }

}
