package vn.com.frankle.karaokelover;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by duclm on 8/8/2016.
 */

public class KAudioRecord {
    private static String DEBUG_TAG = KAudioRecord.class.getSimpleName();

    public interface AudioRecordListener {
        @WorkerThread
        void onAudioRecordDataReceived(byte[] data);

        void onAudioRecordError();
    }

    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_BYTES_ELEMENTS = 1024;
    private static final int BUFFER_BYTES_PER_ELEMENT = RECORDER_AUDIO_ENCODING;
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_SAMPLE_RATE = 44100;

    private AudioRecordListener mDataListener;

    public final AtomicBoolean mIsRecording;

    private ExecutorService mExecutorService;
    private AudioRecord mAudioRecord;
    private final int mShortBufferSize;
    private final int mByteBufferSize;
    private final byte[] mByteBuffer;
    private final short[] mShortBuffer;


    public KAudioRecord(@NonNull AudioRecordListener dataListener) {
        mByteBufferSize = Math.max(BUFFER_BYTES_ELEMENTS * BUFFER_BYTES_PER_ELEMENT,
                AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING));

        mShortBufferSize = mByteBufferSize / 2;
        mDataListener = dataListener;
        mIsRecording = new AtomicBoolean(false);
        mByteBuffer = ByteBuffer.allocate(mByteBufferSize).order(ByteOrder.LITTLE_ENDIAN).array();
        mShortBuffer = new short[mShortBufferSize];

        mAudioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS_IN,
                RECORDER_AUDIO_ENCODING,
                mByteBufferSize
        );
    }

    private class AudioRecordRunnable implements Runnable {

        private String mFilename;

        public AudioRecordRunnable(String filename) {
            this.mFilename = filename;
        }

        @Override
        public void run() {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                try {
                    mAudioRecord.startRecording();
                } catch (IllegalStateException e) {
                    mDataListener.onAudioRecordError();
                    return;
                }

                File recordFileDir = new File(KApplication.Companion.getRECORDING_DIRECTORY_URI());
                if (!recordFileDir.exists()) {
                    recordFileDir.mkdir();
                }
                File recordFile = new File(recordFileDir, mFilename);
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(recordFile);

                    // Write out the wav file header
                    writeWavHeader(os, RECORDER_CHANNELS_IN, RECORDER_SAMPLE_RATE, RECORDER_AUDIO_ENCODING);

                    while (mIsRecording.get()) {
                        int retVal = mAudioRecord.read(mByteBuffer, 0, mByteBufferSize);

                        if (retVal > 0) {
                            os.write(mByteBuffer, 0, mByteBufferSize);
                            mDataListener.onAudioRecordDataReceived(mByteBuffer);
                        } else {
                            mDataListener.onAudioRecordError();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (os != null) {
                        os.close();
                    }
                    updateWavHeader(recordFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mAudioRecord.release();
        }
    }

    /**
     * Start recording voice
     *
     * @param recordFilename : the name of the file to be saved
     * @return
     */
    public synchronized boolean start(String recordFilename) {
        stop();

        mExecutorService = Executors.newSingleThreadExecutor();

        if (mIsRecording.compareAndSet(false, true)) {
            mExecutorService.execute(new AudioRecordRunnable(recordFilename));
            return true;
        }
        return false;
    }

    public synchronized void stop() {
        mIsRecording.compareAndSet(true, false);

        if (mExecutorService != null) {
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }


    private void convertPcmToWav(File pcmFile) {

    }

    /**
     * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
     * Two size fields are left empty/null since we do not yet know the final stream size
     *
     * @param out         The stream to write the header to
     * @param channelMask An AudioFormat.CHANNEL_* mask
     * @param sampleRate  The sample rate in hertz
     * @param encoding    An AudioFormat.ENCODING_PCM_* value
     * @throws IOException
     */
    private static void writeWavHeader(OutputStream out, int channelMask, int sampleRate, int encoding) throws IOException {
        short channels;
        switch (channelMask) {
            case AudioFormat.CHANNEL_IN_MONO:
                channels = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channels = 2;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable channel mask");
        }

        short bitDepth;
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_8BIT:
                bitDepth = 8;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bitDepth = 16;
                break;
            case AudioFormat.ENCODING_PCM_FLOAT:
                bitDepth = 32;
                break;
            default:
                throw new IllegalArgumentException("Unacceptable encoding");
        }

        writeWavHeader(out, channels, sampleRate, bitDepth);
    }

    /**
     * Writes the proper 44-byte RIFF/WAVE header to/for the given stream
     * Two size fields are left empty/null since we do not yet know the final stream size
     *
     * @param out        The stream to write the header to
     * @param channels   The number of channels
     * @param sampleRate The sample rate in hertz
     * @param bitDepth   The bit depth
     * @throws IOException
     */
    private static void writeWavHeader(OutputStream out, short channels, int sampleRate, short bitDepth) throws IOException {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        // Not necessarily the best, but it's very easy to visualize this way
        out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subchunk
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd', 'a', 't', 'a', // Subchunk2ID
                0, 0, 0, 0, // Subchunk2Size (must be updated later)
        });
    }

    /**
     * Updates the given wav file's header to include the final chunk sizes
     *
     * @param wav The wav file to update
     * @throws IOException
     */
    private static void updateWavHeader(File wav) throws IOException {
        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                // There are probably a bunch of different/better ways to calculate
                // these two given your circumstances. Cast should be safe since if the WAV is
                // > 4 GB we've already made a terrible mistake.
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // Subchunk2Size
                .array();

        RandomAccessFile accessWave = null;
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            accessWave = new RandomAccessFile(wav, "rw");
            // ChunkSize
            accessWave.seek(4);
            accessWave.write(sizes, 0, 4);

            // Subchunk2Size
            accessWave.seek(40);
            accessWave.write(sizes, 4, 4);
        } catch (IOException ex) {
            // Rethrow but we still close accessWave in our finally
            throw ex;
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    //
                }
            }
        }
    }
}
