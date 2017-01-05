package vn.com.frankle.karaokelover.activities.states;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import vn.com.frankle.karaokelover.KApplication;
import vn.com.frankle.karaokelover.KAudioRecord;

/**
 * Created by duclm on 03-Jan-17.
 */

public class KAudioRecordHighQualityState extends KAudioRecordBaseState {

    private static final int RECORDER_SAMPLE_RATE = 44100;
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_BYTES_ELEMENTS = 1024;
    private static final int BUFFER_BYTES_PER_ELEMENT = RECORDER_AUDIO_ENCODING;

    private AudioRecord mAudioRecord;

    private final int mByteBufferSize;
    private final byte[] mByteBuffer;

    public KAudioRecordHighQualityState(KAudioRecord audioRecordInstance) {
        super(audioRecordInstance);

        mByteBufferSize = Math.max(BUFFER_BYTES_ELEMENTS * BUFFER_BYTES_PER_ELEMENT,
                AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING));

        mByteBuffer = ByteBuffer.allocate(mByteBufferSize).order(ByteOrder.LITTLE_ENDIAN).array();
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

    private class HighQualityAudioRecordRunnable implements Runnable {

        private String mFilename;
        private boolean mIsResume;

        public HighQualityAudioRecordRunnable(String filename, boolean isResume) {
            this.mFilename = filename;
            this.mIsResume = isResume;
        }

        @Override
        public void run() {
            mAudioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    RECORDER_SAMPLE_RATE,
                    RECORDER_CHANNELS_IN,
                    RECORDER_AUDIO_ENCODING,
                    mByteBufferSize
            );

            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                File recordFileDir = new File(KApplication.Companion.getRECORDING_DIRECTORY_URI());
                boolean createDirSuccess = true;
                if (!recordFileDir.exists()) {
                    createDirSuccess = recordFileDir.mkdir();
                }
                if (createDirSuccess) {
                    File recordFile = new File(recordFileDir, mFilename);
                    try {
                        mAudioRecord.startRecording();
                    } catch (IllegalStateException e) {
                        mAudioRecordInstance.getAudioRecordListener().onAudioRecordError();
                        return;
                    }

                    try {
                        FileOutputStream os;
                        if (mIsResume) {
                            os = new FileOutputStream(recordFile, true);
                        } else {
                            os = new FileOutputStream(recordFile, false);
                        }
                        // Write out the wav file header
                        writeWavHeader(os, RECORDER_CHANNELS_IN, RECORDER_SAMPLE_RATE, RECORDER_AUDIO_ENCODING);

                        while (mAudioRecordInstance.isRecording()) {
                            int retVal = mAudioRecord.read(mByteBuffer, 0, mByteBufferSize);

                            if (retVal > 0) {
                                os.write(mByteBuffer, 0, mByteBufferSize);
                                mAudioRecordInstance.getAudioRecordListener().onAudioRecordDataReceived(mByteBuffer, retVal);
                            } else {
                                mAudioRecordInstance.getAudioRecordListener().onAudioRecordError();
                            }
                        }

                        os.close();

                        updateWavHeader(recordFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mAudioRecordInstance.getAudioRecordListener().onAudioRecordError();
                }
            }
        }
    }


    @Override
    public Runnable getAudioRecordRunnable(String recordFilename, boolean isResume) {
        return new HighQualityAudioRecordRunnable(recordFilename, isResume);
    }

    @Override
    public void stopRecording() {
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }
}