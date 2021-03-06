package org.logstash.ackedqueue;

import org.logstash.ackedqueue.io.PageIO;

import java.io.IOException;
import java.util.BitSet;

class PageFactory {

    /**
     * create a new head page object and new page.{@literal {pageNum}} empty valid data file
     *
     * @param pageNum the new head page page number
     * @param queue the {@link Queue} instance
     * @param pageIO the {@link PageIO} delegate
     * @return {@link Page} the new head page
     */
    public static Page newHeadPage(int pageNum, Queue queue, PageIO pageIO) {
        return new Page(pageNum, queue, 0, 0, 0, new BitSet(), pageIO, true);
    }

    /**
     * create a new head page from an existing {@link Checkpoint} and open page.{@literal {pageNum}} empty valid data file
     *
     * @param checkpoint existing head page {@link Checkpoint}
     * @param queue the {@link Queue} instance
     * @param pageIO the {@link PageIO} delegate
     * @return {@link Page} the new head page
     */
    public static Page newHeadPage(Checkpoint checkpoint, Queue queue, PageIO pageIO) throws IOException {
        final Page p = new Page(
                checkpoint.getPageNum(),
                queue,
                checkpoint.getMinSeqNum(),
                checkpoint.getElementCount(),
                checkpoint.getFirstUnackedSeqNum(),
                new BitSet(),
                pageIO,
                true
        );
        try {
            assert checkpoint.getMinSeqNum() == pageIO.getMinSeqNum() && checkpoint.getElementCount() == pageIO.getElementCount() :
                    String.format("checkpoint minSeqNum=%d or elementCount=%d is different than pageIO minSeqNum=%d or elementCount=%d", checkpoint.getMinSeqNum(), checkpoint.getElementCount(), pageIO.getMinSeqNum(), pageIO.getElementCount());

            // this page ackedSeqNums bitset is a new empty bitset, if we have some acked elements, set them in the bitset
            if (checkpoint.getFirstUnackedSeqNum() > checkpoint.getMinSeqNum()) {
                p.ackedSeqNums.flip(0, (int) (checkpoint.getFirstUnackedSeqNum() - checkpoint.getMinSeqNum()));
            }

            return p;
        } catch (Exception e) {
            p.close();
            throw e;
        }
    }

    /**
     * create a new tail page for an exiting Checkpoint and data file
     *
     * @param checkpoint existing tail page {@link Checkpoint}
     * @param queue the {@link Queue} instance
     * @param pageIO the {@link PageIO} delegate
     * @return {@link Page} the new tail page
     */
    public static Page newTailPage(Checkpoint checkpoint, Queue queue, PageIO pageIO) throws IOException {
        final Page p = new Page(
                checkpoint.getPageNum(),
                queue,
                checkpoint.getMinSeqNum(),
                checkpoint.getElementCount(),
                checkpoint.getFirstUnackedSeqNum(),
                new BitSet(),
                pageIO,
                false
        );

        try {
            // this page ackedSeqNums bitset is a new empty bitset, if we have some acked elements, set them in the bitset
            if (checkpoint.getFirstUnackedSeqNum() > checkpoint.getMinSeqNum()) {
                p.ackedSeqNums.flip(0, (int) (checkpoint.getFirstUnackedSeqNum() - checkpoint.getMinSeqNum()));
            }

            return p;
        } catch (Exception e) {
            p.close();
            throw e;
        }
    }

}
