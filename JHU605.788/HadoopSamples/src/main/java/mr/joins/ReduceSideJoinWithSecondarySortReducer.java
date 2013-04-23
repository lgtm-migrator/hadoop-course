package mr.joins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mr.joins.support.TextPair;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReduceSideJoinWithSecondarySortReducer extends Reducer<TextPair, TextPair, Text, Text> {

    private final List<Text> leftSide = new ArrayList<Text>();
    private final Logger log = LoggerFactory.getLogger(ReduceSideJoinWithSecondarySortReducer.class);
    @Override
    protected void reduce(TextPair tuppleKey, Iterable<TextPair> joined, Context context) throws IOException,
            InterruptedException {
        log.debug("Processing key [{}]",tuppleKey );
        leftSide.clear();
        for (TextPair t : joined) {
            log.debug("Processing value [{}]", t);
            Text val = t.getFirst();
            Text indicator = t.getSecond();
            if (indicator.toString().equals("L")) {
                log.debug("Left key [{}]", t);
                leftSide.add(val);
            } else if (leftSide.isEmpty()){
                log.debug("No join");
                return;
            } else {
                for (Text left : leftSide) {
                    log.debug("Emiting [{}] [{}]", left, val);
                    context.write(left, val);
                }
            }
        }
    }
    
    public static class ReduceSidePartitioner extends Partitioner<TextPair, TextPair>{
        @Override
        public int getPartition(TextPair key, TextPair TextPair, int numPartitions) {
            return (key.getFirst().hashCode() & Integer.MAX_VALUE) % numPartitions;
        }
    }
    public static class ReduceSideGroupComparator extends WritableComparator{
        private final Logger log = LoggerFactory.getLogger(ReduceSideGroupComparator.class);
        protected ReduceSideGroupComparator() {
            super(TextPair.class, true);
        }
        @SuppressWarnings("rawtypes")
        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            TextPair t1 = (TextPair)a;
            TextPair t2 = (TextPair) b;
            int res = t1.getFirst().compareTo(t2.getFirst());
            log.debug(t1 + " <> " + t2 + " = " + res);
            return res;
        }
    }
}
