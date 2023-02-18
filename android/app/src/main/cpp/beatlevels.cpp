// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("beatlevels");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("beatlevels")
//      }
//    }
#define LOG_TAG "A_TAG"

#include <iostream>
#include <fstream>
#include "jni.h"
#include <android/log.h>

extern "C" {

#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswresample/swresample.h"
#include "libavutil/audio_fifo.h"

#define AUDIO_INBUF_SIZE 20480
#define AUDIO_REFILL_THRESH 4096

int decode_to_pcm(const char *outfilename, const char *filename) {
    try {


        /* Open File */
        AVFormatContext * format_ctx{nullptr};
        int result_open = avformat_open_input(&format_ctx, filename, nullptr, nullptr);
        if(result_open < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "exception: ", "%s", av_err2str(result_open));
            return -1;
        }

        result_open = avformat_find_stream_info(format_ctx, nullptr);
        if(result_open < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "exception: ", "%s", "Failed to read stream info");
            return -1;
        }

        __android_log_print(ANDROID_LOG_INFO, "Number of Streams: ", "%u", format_ctx->nb_streams);

        int index = av_find_best_stream(format_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
        if(index < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "exception: ", "%s", "No audio stream inside the file");
            return  -1;
        }

        /* Finding decoder */
        AVStream *streams = format_ctx->streams[index];
        const AVCodec *decoder = avcodec_find_decoder(streams->codecpar->codec_id);
        if(!decoder) {
            __android_log_print(ANDROID_LOG_ERROR, "exception: ", "%s", "No decoders available for the audio format");
            return  -1;
        }

        AVCodecContext *codec_ctx{avcodec_alloc_context3(decoder)};

        avcodec_parameters_to_context(codec_ctx, streams->codecpar);

        /* Opening decoder */
        result_open = avcodec_open2(codec_ctx, decoder, nullptr);
        if(result_open < 0) {
            __android_log_print(ANDROID_LOG_ERROR, "exception: ", "%s", "Failed to open decoder");
            return  -1;
        }

        /* Decoding the audio */
        AVPacket *packet = av_packet_alloc();
        AVFrame *frame = av_frame_alloc();

        SwrContext *resampler{swr_alloc_set_opts(
                nullptr,
                streams->codecpar->channel_layout,
                AV_SAMPLE_FMT_FLT,
                streams->codecpar->sample_rate,
                streams->codecpar->channel_layout,
                (AVSampleFormat) streams->codecpar->format,
                streams->codecpar->format,
                streams->codecpar->sample_rate,
                0
        )};

        std::ofstream out(outfilename, std::ios::binary);
        while (av_read_frame(format_ctx, packet) == 0) {
            if(packet->stream_index != streams->index) {
                continue;
            }

            result_open = avcodec_send_packet(codec_ctx, packet);
            if(result_open < 0) {
                // AVERROR(EAGAIN) --> Send the packet again getting frames out!
                if(result_open != AVERROR(EAGAIN)) {
                    __android_log_print(ANDROID_LOG_ERROR, "exception: ", "%s", "Error decoding...");
                }
            }
            while (avcodec_receive_frame(codec_ctx, frame) == 0) {
                /* Resample the frame */
                AVFrame *resampler_frame = av_frame_alloc();
                resampler_frame->sample_rate = 100;
                resampler_frame->channel_layout = frame->channel_layout;
                resampler_frame->channels = frame->channels;
                resampler_frame->format = AV_SAMPLE_FMT_S16;

                result_open = swr_convert_frame(resampler, resampler_frame, frame);
                if(result_open >= 0) {
                    int16_t *samples = (int16_t *) frame->data[0];
                    for(int c = 0; c < resampler_frame->channels; c ++) {
                        float sum = 0;
                        for(int i = 0; i < resampler_frame->nb_samples; i ++) {
                            if(samples[i * resampler_frame->channels + c] < 0) {
                                sum += (float) samples[i * resampler_frame->channels + c] * (-1);
                            } else {
                                sum += (float) samples[i * resampler_frame->channels + c];
                            }
                            int average_point = (int) ((sum * 2) / (float) resampler_frame->nb_samples);
                            if(average_point > 0) {
                                out << average_point << "\n";
                            }
                        }
                    }
                }
                av_frame_unref(frame);
                av_frame_free(&resampler_frame);
            }
        }
        out.close();
        return 0;
    } catch (std::exception exception) {
        __android_log_print(ANDROID_LOG_ERROR, "exception: ", "%s", exception.what());
    }
    return 0;
}

/* Waveform Reducer */
short * get_waveform(const char* pcm, int wave_size) {
    short array[wave_size];
    /* counting lines */
    std::ifstream inFile(pcm);
    unsigned long count_lines = std::count(std::istreambuf_iterator<char>(inFile),std::istreambuf_iterator<char>(), '\n');
    inFile.close();

    /* Getting waves */
    std::ifstream input( pcm);
    count_lines = count_lines - 1;
    int length = count_lines / wave_size;
    long sum = 0;
    long x = 0;
    int i = 0;
    long length_sum = length;
    for(std::string line; getline( input, line);) {
        short value;
        sscanf(line.c_str(), "%hd", &value);
        if(x < length_sum) {
            sum += value;
        } else {
            array[i] = sum / length;
            sum = 0;
            length_sum += length;
            i ++;
        }
        x ++;
    }
    input.close();
    remove(pcm);
    return array;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_znbox_beatlevels_modules_Waveform_decode_1to_1pcm(JNIEnv *env, jobject thiz, jstring input, jstring output) {
    int wave_size = 100;
    short _res[0];
    const char * _input = (*env).GetStringUTFChars(input, 0);
    const char * _output = (*env).GetStringUTFChars(output, 0);
    int ret = decode_to_pcm(strdup(_output), strdup(_input));
    return ret;
}
};