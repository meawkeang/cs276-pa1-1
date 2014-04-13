package cs276.assignments;

import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class BasicIndex implements BaseIndex {
    /*
        BasicIndex stores PostingLists as a series of integers.
        We track the length of each list globally via doc frequency
        for each term. We track the offset into the Index record for
        each term globally so that we can advance file pointers to
        appropriate places before reading out the list.
    */

    @Override
    public PostingList readPosting(FileChannel fc) {
        /*
          Assumes fc has been advanced to an appropriate offset into
          the file representing the beginning of a list. Given how
          long the postings list is, we read exactly that many
          integers from the file stream, and interpret this bytes
          as a postingslist.

          Returns null on EOF encountered or IOException
        */
        try {
          ByteBuffer bb = ByteBuffer.allocate(8);

          int bytesRead = fc.read(bb);
          if (bytesRead == -1) return null;
          int termId = bb.getInt();
          int docFreq = bb.getInt();

          ArrayList<Integer> list = new ArrayList<Integer>();

          ByteBuffer docBuffer = ByteBuffer.allocate(4 * docFreq);
          bytesRead = fc.read(docBuffer);
          if (bytesRead == -1) return null;
          for (int i = 0; i < docFreq; i++) {
            list.add(bb.getInt());
          }

          return new PostingList(termId, list);
        }
        catch (IOException e) {
          return null;
        }
    }

    @Override
    public void writePosting(FileChannel fc, PostingList p) {
        /*
          Assumes we've recorded the offset into the stream;
          writes the termID followed by each integer in the
          posting list into the byte stream. Assumes that the
          offset after all the writes have been done will also be
          recorded.
        */
        try {
          int termId = p.getTermId();
          List<Integer> docList = p.getList();

          ByteBuffer bb = ByteBuffer.allocate(8);
          bb.putInt(termId);
          bb.putInt(docList.size());

          fc.write(bb);

          ByteBuffer docBuf = ByteBuffer.allocate(4 * docList.size());
          for (Integer docId: docList) {
            bb.putInt(docId);
          }
          fc.write(docBuf);
        }
        catch (IOException e) {}
    }
}
