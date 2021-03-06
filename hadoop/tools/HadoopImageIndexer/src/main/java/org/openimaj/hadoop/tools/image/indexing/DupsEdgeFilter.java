/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.hadoop.tools.image.indexing;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.mapreduce.TextBytesJobUtil;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;

public class DupsEdgeFilter extends Configured implements Tool {
	static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException
		{
			final String[] parts = value.toString().split("\\s");

			if (parts.length == 3) {
				final int count = Integer.parseInt(parts[2]);

				if (count > 10) {
					if (parts[0].compareTo(parts[1]) < 0)
						context.write(new Text(parts[0] + " " + parts[1]), new IntWritable(count));
					else
						context.write(new Text(parts[1] + " " + parts[0]), new IntWritable(count));
				}
			}
		}
	}

	static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
				InterruptedException
		{
			int count = 0;
			for (final IntWritable v : values) {
				count += v.get();
			}
			context.write(key, new IntWritable(count));
		}
	}

	@Option(
			name = "--remove",
			aliases = "-rm",
			required = false,
			usage = "Remove the existing output location if it exists.",
			metaVar = "BOOLEAN")
	private boolean replace = false;

	@Option(name = "--input", aliases = "-i", required = true, usage = "Input graph edges file.", metaVar = "STRING")
	private String input;

	@Option(name = "--output", aliases = "-o", required = true, usage = "Output graph edges file.", metaVar = "STRING")
	private String output;

	@Override
	public int run(String[] args) throws Exception {
		final CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: hadoop jar HadoopImageIndexer.jar [options]");
			parser.printUsage(System.err);
			return -1;
		}

		final Path[] paths = SequenceFileUtility.getFilePaths(input, "part");
		final Path outputPath = new Path(output);

		if (outputPath.getFileSystem(this.getConf()).exists(outputPath) && replace)
			outputPath.getFileSystem(this.getConf()).delete(outputPath, true);

		final Job job = TextBytesJobUtil.createJob(paths, outputPath, null, this.getConf());
		job.setJarByClass(this.getClass());

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setCombinerClass(Reduce.class);
		job.setNumReduceTasks(1);

		FileOutputFormat.setCompressOutput(job, false);

		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ToolRunner.run(new DupsEdgeFilter(), args);
	}
}
