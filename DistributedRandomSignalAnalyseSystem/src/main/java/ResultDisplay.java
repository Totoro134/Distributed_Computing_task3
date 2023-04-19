import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.ArrayList;
import tech.tablesaw.api.Table;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.LinePlot;

class ResultListener implements MessageListener {
    private int count = 0;
    ArrayList<Double>[] value = new ArrayList[5];
    ArrayList<String>[] name = new ArrayList[5];
    ArrayList<Integer> index = new ArrayList<Integer>();

    ResultListener(){
        for(int i = 0; i < 5; i++){
            value[i] = new ArrayList<Double>();
            name[i] = new ArrayList<String>();
        }
    }

    public void onMessage(Message message) {
        TextMessage textmessage = (TextMessage)message;
        try {
            String msg = String.valueOf(textmessage.getText());
            String[] result = msg.split(" ");
            count++;
            ArrayList<Integer> index_total = new ArrayList<Integer>();
            ArrayList<Double> value_total = new ArrayList<Double>();
            ArrayList<String> name_total = new ArrayList<String>();
            index.add(Integer.valueOf(result[1]));
            name[0].add("新增的信号值");
            name[1].add("最近 "+Integer.valueOf(result[0])+" 个信号的平均值");
            name[2].add("最近 "+Integer.valueOf(result[0])+" 个信号的方差");
            name[3].add("信号最小值");
            name[4].add("信号最大值");
            for(int i = 0; i < 5; i++){
                value[i].add(Double.valueOf(result[i + 2]));
                index_total.addAll(index);
                value_total.addAll(value[i]);
                name_total.addAll(name[i]);
            }
            System.out.println("第 "+count+" 次分析：共"+Integer.valueOf(result[1])+"个信号，最近 "
                    +Integer.valueOf(result[0])+" 个信号平均值: "+String.format("%.4f",Double.valueOf(result[3]))
                    +", 方差: "+String.format("%.4f",Double.valueOf(result[4]))+"，所有信号最小值: "
                    + String.format("%.4f",Double.valueOf(result[5]))+"，最大值:"+String.format("%.4f",Double.valueOf(result[6])));

            Table tab = Table.create("随机信号").addColumns(DoubleColumn.create("序号", index_total), DoubleColumn.create("值", value_total), StringColumn.create("分类", name_total));

            Plot.show(LinePlot.create("分布式随机信号分析折线图", tab, "序号", "值", "分类"));
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class ResultDisplay {
    public static void main(String[] args) throws JMSException {
        String brokerURL = "tcp://localhost:61616";
        ConnectionFactory factory = null;
        Connection connection = null;
        Session session = null;
        Destination destination = null;
        MessageConsumer messageConsumer = null;
        ResultListener listener = null;
        try {
            factory = new ActiveMQConnectionFactory(brokerURL);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue("Result");
            messageConsumer = session.createConsumer(destination);
            listener = new ResultListener();
            messageConsumer.setMessageListener(listener);

            System.out.println("结果分析启动，按下回车以退出...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
}
